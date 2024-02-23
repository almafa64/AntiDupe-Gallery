#![allow(unused)]

mod android;
mod hash;
mod logger;

use std::path::PathBuf;
use std::sync::atomic::{AtomicBool, AtomicU64, AtomicUsize, Ordering};

use jni::JNIEnv;
use sqlx::query::QueryAs;
use sqlx::sqlite::{SqliteArguments, SqlitePoolOptions};
use sqlx::{Database, Executor, Pool, Sqlite};
use tokio::sync::{mpsc, oneshot};
use futures_util::TryStreamExt;

use std::sync::Arc;

enum Message {
    RunHashProcess {
        chash: bool,
        phash: bool,
        stop_recver: oneshot::Receiver<()>,
    },
}

#[derive(Debug, Default)]
struct Shared {
    hash_status: HashStatus,
}

async fn main(
    mut message_recver: mpsc::UnboundedReceiver<Message>,
    mut shutdown_recver: oneshot::Receiver<()>,
    mut shared_state: Arc<Shared>,
    db_path: &str,
) {
    let db = SqlitePoolOptions::new()
        .connect(&format!("sqlite://{db_path}?mode=rw"))
        .await
        .unwrap();

    loop {
        tokio::select! {
            message = message_recver.recv() => {
                match message {
                    Some(message) => match message {
                        Message::RunHashProcess { chash, phash, mut stop_recver } => {
                            log::info!("RunHashProcess received");
                            tokio::select! {
                                _ = hash_start(&db, &shared_state, chash, phash) => {}
                                _ = &mut stop_recver => {
                                    log::info!("Backend hash stop signal received");
                                }
                            }
                            shared_state.hash_status.running.store(false, Ordering::Relaxed);
                        },
                    },
                    None => panic!("Message sender closed"),
                }
            }
            _ = &mut shutdown_recver => {
                log::info!("Backend shutdown");
                break;
            }
        }
    }
}

#[derive(Debug, Default)]
struct HashStatus {
    total_count: AtomicU64,
    completed: AtomicU64,
    running: AtomicBool,
}

type CHashMediaRow = (i64, Box<str>, i64);
type PHashMediaRow = (i64, Box<str>, i64);

async fn hash_start(db: &Pool<Sqlite>, shared: &Arc<Shared>, chash: bool, phash: bool) {
    let chash_count = if chash {
        let (count,): (i32,) = sqlx::query_as(
            "SELECT COUNT(*) \
            FROM media \
            LEFT JOIN chash \
                ON chash.media_id = media.id \
            WHERE \
                chash.media_id IS NULL \
                    OR chash.calc_mtime < media.mtime \
            ")
            .fetch_one(db)
            .await
            .unwrap();
        count as u64
    } else {
        0
    };

    let phash_count = if phash {
        let (count,): (i32,) = sqlx::query_as(
            "SELECT COUNT(*) \
            FROM media \
            LEFT JOIN phash \
                ON phash.media_id = media.id \
            WHERE \
                (phash.media_id IS NULL \
                    OR phash.calc_mtime < media.mtime) \
                AND media.mimeType = 1 \
            ")
            .fetch_one(db)
            .await
            .unwrap();
        count as u64
    } else {
        0
    };

    let total_count = chash_count + phash_count;
    shared.hash_status.total_count.store(total_count, Ordering::Relaxed);
    shared.hash_status.completed.store(0, Ordering::Relaxed);
    shared.hash_status.running.store(true, Ordering::Relaxed);

    if chash {
        do_chash(db, shared).await;
    }

    if phash {
        do_phash(db, shared).await;
    }
}

async fn do_chash(db: &Pool<Sqlite>, shared: &Arc<Shared>) {
    let mut query = sqlx::query_as(
        "SELECT id, path, mtime \
        FROM media \
        LEFT JOIN chash \
            ON chash.media_id = media.id \
        WHERE \
            chash.media_id IS NULL
                OR chash.calc_mtime < media.mtime \
        ")
        .fetch(db);

    while let Some((id, path, mtime)) = query.try_next().await.unwrap() {
        let (id, path, mtime): CHashMediaRow = (id, path, mtime);

        match hash::chash(&*path) {
            Ok(bytes) => {
                sqlx::query("INSERT OR REPLACE INTO chash (media_id, bytes, calc_mtime) VALUES (?,?,?)")
                    .bind(id)
                    .bind(&bytes[..])
                    .bind(mtime)
                    .execute(db)
                    .await
                    .expect("Query to database failed");
                shared.hash_status.completed.fetch_add(1, Ordering::Relaxed);
            },
            Err(err) => log::error!("chash failed for {path}: {err}"),
        }
    }
}

async fn do_phash(db: &Pool<Sqlite>, shared: &Arc<Shared>) {
    let mut query = sqlx::query_as(
        "SELECT id, path, mtime \
        FROM media \
        LEFT JOIN phash \
            ON phash.media_id = media.id \
        WHERE \
            (phash.media_id IS NULL \
                OR phash.calc_mtime < media.mtime) \
            AND media.mimeType = 1
        ")
        .fetch(db);

    while let Some((id, path, mtime)) = query.try_next().await.unwrap() {
        let (id, path, mtime): PHashMediaRow = (id, path, mtime);

        match hash::phash(&*path) {
            Ok(bytes) => {
                sqlx::query("INSERT OR REPLACE INTO phash (media_id, bytes, calc_mtime) VALUES (?,?,?)")
                    .bind(id)
                    .bind(&bytes[..])
                    .bind(mtime)
                    .execute(db)
                    .await
                    .expect("Query to database failed");
                shared.hash_status.completed.fetch_add(1, Ordering::Relaxed);
            },
            Err(err) => log::error!("phash failed for {path}: {err}"),
        }
    }
}

impl HashStatus {
    pub fn total_count(&self) -> u64 {
        self.total_count.load(Ordering::Relaxed)
    }

    pub fn completed(&self) -> u64 {
        self.completed.load(Ordering::Relaxed)
    }

    pub fn running(&self) -> bool {
        self.running.load(Ordering::Relaxed)
    }
}
