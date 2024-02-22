use std::path::PathBuf;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::{OnceLock, RwLock, Arc};
use std::thread::JoinHandle;
use std::{env, panic, thread};

use backend_proc_macro::java;
use jni::objects::{GlobalRef, JClass, JObject, JString, JValueGen};
use jni::sys::{jboolean, jint, jlong, JNI_VERSION_1_6};
use jni::{JNIEnv, JavaVM};
use sqlx::sqlite::SqlitePoolOptions;
use sqlx::Database;
use tokio::sync::{mpsc, oneshot};

use crate::{Message, Shared};

use super::log;

struct Context {
    main_thread: JoinHandle<()>,
    logger_thread: JoinHandle<()>,
    msg_sender: mpsc::UnboundedSender<Message>,
    shutdown_sender: Option<oneshot::Sender<()>>,
    shared: Arc<Shared>,
    hash_stop_sender: Option<oneshot::Sender<()>>,
}

static CONTEXT: OnceLock<RwLock<Context>> = OnceLock::new();

#[java(export(class = "com.cyberegylet.antiDupeGallery.backend.Backend"))]
pub extern "C" fn init(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) {
    log::info(&mut env, "Backend", "Backend init called");

    let (msg_sender, msg_recver) = mpsc::unbounded_channel();
    let (shutdown_sender, shutdown_recver) = oneshot::channel();
    let shared = Arc::new(Shared::default());
    let db_path = env
        .get_string(&db_path)
        .unwrap()
        .to_str()
        .unwrap()
        .to_owned();

    log::info(
        &mut env,
        "Backend",
        format!("using database file: {db_path}"),
    );

    let main_thread = {
        let vm = env.get_java_vm().expect("Failed to get JVM");
        let shared = shared.clone();
        thread::spawn(move || {
            let env_guard = vm
                .attach_current_thread()
                .expect("Failed to attach thread to JVM");
            let env = unsafe { env_guard.unsafe_clone() };
            use tokio::runtime::Builder;
            let runtime = Builder::new_current_thread()
                .enable_all()
                .build()
                .expect("Failed to build tokio runtime");
            runtime.block_on(crate::main(
                msg_recver,
                shutdown_recver,
                shared,
                &db_path,
            ));
        })
    };

    let logger_thread = {
        let vm = env.get_java_vm().expect("Failed to get JVM");
        thread::spawn(move || {
            let env_guard = vm
                .attach_current_thread()
                .expect("Failed to attach thread to JVM");
            let mut env = unsafe { env_guard.unsafe_clone() };

            let (tx, mut rx) = mpsc::unbounded_channel();
            crate::logger::Logger::init(tx);

            loop {
                let msg = rx.blocking_recv();
                if let Some(msg) = msg {
                    use ::log::Level as L;
                    match msg.0 {
                        L::Error => log::err(&mut env, "Backend", msg.1),
                        L::Warn => log::warn(&mut env, "Backend", msg.1),
                        L::Info => log::info(&mut env, "Backend", msg.1),
                        L::Debug | L::Trace => log::debug(&mut env, "Backend", msg.1),
                    }
                } else {
                    break;
                }
            }
        })
    };
    
    CONTEXT
        .set(RwLock::new(Context {
            main_thread,
            logger_thread,
            msg_sender,
            shutdown_sender: Some(shutdown_sender),
            shared,
            hash_stop_sender: None,
        }))
        .or_else::<(), _>(|_| panic!("Backend.init may only be called once"))
        .unwrap();
}

#[java(export(class = "com.cyberegylet.antiDupeGallery.backend.Backend", rename = "runHashProcess"))]
pub extern "C" fn run_hash_process<'a>(_env: JNIEnv<'a>, _class: JClass, chash: jboolean, phash: jboolean) {
    let mut context = CONTEXT.get().unwrap().write().unwrap();
    if context.hash_stop_sender.is_none() {
        let (tx, rx) = oneshot::channel();
        context.hash_stop_sender = Some(tx);
        context.msg_sender.send(Message::RunHashProcess { chash: chash != 0, phash: phash != 0, stop_recver: rx });
    }
}

#[java(export(class = "com.cyberegylet.antiDupeGallery.backend.Backend", rename = "stopHashProcess"))]
pub extern "C" fn stop_hash_process<'a>(_env: JNIEnv<'a>, _class: JClass) {
    let mut context = CONTEXT.get().unwrap().write().unwrap();
    if let Some(hash_stop_sender) = context.hash_stop_sender.take() {
        hash_stop_sender.send(()).unwrap();
    }
}

#[java(export(class = "com.cyberegylet.antiDupeGallery.backend.Backend", rename = "getHashStatus"))]
pub extern "C" fn get_hash_status<'a>(mut env: JNIEnv<'a>, _class: JClass) -> JObject<'a> {
    const HASH_STATUS_CLASS: &'static str = "com/cyberegylet/antiDupeGallery/backend/Backend$HashStatus";
    const HASH_STATUS_CTOR_SIG: &'static str = "(JJ)V"; // (long, long) -> void

    let context = CONTEXT.get().unwrap().read().unwrap();

    let obj = env.new_object(HASH_STATUS_CLASS, HASH_STATUS_CTOR_SIG, &[
        JValueGen::Long(context.shared.hash_status.total_count().try_into().unwrap()),
        JValueGen::Long(context.shared.hash_status.completed().try_into().unwrap()),
    ]);

    let obj = obj.expect("Failed to create HashStatus object");

    obj
}

#[java(export(class = "com.cyberegylet.antiDupeGallery.backend.Backend"))]
pub extern "C" fn shutdown(_env: JNIEnv, _class: JClass) {
    let mut ctx = CONTEXT.get().unwrap().write().unwrap();
    ctx.shutdown_sender.take().unwrap().send(());
}
