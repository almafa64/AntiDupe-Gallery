#![allow(unused)]

mod android;
mod digest;
mod logger;

use std::path::PathBuf;
use std::sync::atomic::{AtomicUsize, Ordering};

use jni::JNIEnv;
use sqlx::sqlite::SqlitePoolOptions;
use sqlx::{Database, Executor, Pool, Sqlite};
use tokio::sync::{mpsc, oneshot};

use std::sync::Arc;

async fn main(
    mut file_recver: mpsc::UnboundedReceiver<(i64, PathBuf)>,
    mut shutdown_recver: oneshot::Receiver<()>,
    mut file_queue_length: Arc<AtomicUsize>,
    db_path: &str,
) {
    let db = SqlitePoolOptions::new()
        .connect(&format!("sqlite://{db_path}?mode=rw"))
        .await
        .unwrap();

    loop {
        tokio::select! {
            file = file_recver.recv() => {
                if let Some((id, path)) = file {
                    handle_file(&db, (id, path)).await;
                    file_queue_length.fetch_sub(1, Ordering::SeqCst);
                }
            }
            _ = &mut shutdown_recver => {
                break;
            }
        }
    }
}

async fn handle_file(
    db: &Pool<Sqlite>,
    file: (i64, PathBuf),
) {
    let (id, path) = file;
    let (exists,): (bool,) =
        sqlx::query_as("SELECT EXISTS(SELECT 1 FROM digests WHERE id = ?)")
            .bind(id)
            .fetch_one(db)
            .await
            .unwrap();
    if !exists {
        match digest::digest(&path) {
            Ok(digest) => {
                let path = path.as_os_str().to_str().unwrap();
                sqlx::query("INSERT INTO digests VALUES (?, ?, ?)")
                    .bind(id)
                    .bind(path)
                    .bind(&digest.digest[..])
                    .execute(db)
                    .await
                    .unwrap();
            }
            Err(err) => {
                log::error!("digest::digest failed: {err}");
            }
        }
    }
}
