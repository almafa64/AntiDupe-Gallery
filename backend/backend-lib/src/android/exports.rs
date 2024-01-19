use std::path::PathBuf;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::{OnceLock, RwLock, Arc};
use std::thread::JoinHandle;
use std::{env, panic, thread};

use backend_proc_macro::java;
use jni::objects::{GlobalRef, JClass, JObject, JString, JValueGen};
use jni::sys::{jint, jlong, JNI_VERSION_1_6};
use jni::{JNIEnv, JavaVM};
use sqlx::sqlite::SqlitePoolOptions;
use sqlx::Database;
use tokio::sync::{mpsc, oneshot};

use super::log;

struct Context {
    main_thread: JoinHandle<()>,
    logger_thread: JoinHandle<()>,
    main_activity: GlobalRef,
    file_sender: mpsc::UnboundedSender<(i64, PathBuf)>,
    shutdown_sender: Option<oneshot::Sender<()>>,
    file_queue_length: Arc<AtomicUsize>,
}

static CONTEXT: OnceLock<RwLock<Context>> = OnceLock::new();

#[java(export(class = "com.cyberegylet.antiDupeGallery.backend.Backend"))]
pub extern "C" fn init(
    mut env: JNIEnv,
    _class: JClass,
    main_activity: JObject,
) {
    log::info(&mut env, "Backend", "Backend init called");
    let main_activity = env
        .new_global_ref(main_activity)
        .expect("Can't create global ref to MainActivity");
    let (file_sender, file_recver) = mpsc::unbounded_channel();
    let (shutdown_sender, shutdown_recver) = oneshot::channel();
    let file_queue_length = Arc::new(AtomicUsize::new(0));
    let file_queue_length2 = file_queue_length.clone();
    let db_path = get_db_path(&mut env, &main_activity, "data.db");
    log::info(
        &mut env,
        "Backend",
        format!("using database file: {db_path}"),
    );
    let vm = env.get_java_vm().expect("Failed to get JVM");
    let main_thread = thread::spawn(move || {
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
            file_recver,
            shutdown_recver,
            file_queue_length2,
            &db_path,
        ));
    });
    let vm2 = env.get_java_vm().expect("Failed to get JVM"); // ain't no way 😭
    let logger_thread = thread::spawn(move || {
        let env_guard = vm2
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
    });
    CONTEXT
        .set(RwLock::new(Context {
            main_thread,
            logger_thread,
            main_activity,
            file_sender,
            shutdown_sender: Some(shutdown_sender),
            file_queue_length,
        }))
        .or_else::<(), _>(|_| panic!("Backend.init may only be called once"))
        .unwrap();
}

#[java(export(
    class = "com.cyberegylet.antiDupeGallery.backend.Backend",
    rename = "queueFile"
))]
pub extern "C" fn queue_file<'o>(
    mut env: JNIEnv<'o>,
    _class: JClass,
    id: jlong,
    path: JString,
) {
    let path = env.get_string(&path).unwrap();
    let path = path.to_str().unwrap();
    let path = PathBuf::from(path);

    let mut ctx = CONTEXT.get().unwrap().write().unwrap();

    ctx.file_queue_length.fetch_add(1, Ordering::SeqCst);

    ctx.file_sender.send((id, path));
}

#[java(export(class = "com.cyberegylet.antiDupeGallery.backend.Backend"))]
pub extern "C" fn shutdown(mut env: JNIEnv, _class: JClass) {
    let mut ctx = CONTEXT.get().unwrap().write().unwrap();
    ctx.shutdown_sender.take().unwrap().send(());
}

fn get_db_path(
    env: &mut JNIEnv,
    main_activity: &JObject,
    db_name: &str,
) -> String {
    let db_name = env.new_string(db_name).unwrap();

    let JValueGen::Object(path) = env
        .call_method(
            main_activity,
            "getDbPath",
            "(Ljava/lang/String;)Ljava/lang/String;",
            &[JValueGen::Object(&db_name)],
        )
        .expect("Call to getDbPath failed")
    else {
        panic!("getDbPath didn't return an object");
    };

    assert!(env.is_instance_of(&path, "java/lang/String").unwrap());
    let path = JString::from(path);

    let path = env.get_string(&path).unwrap();
    let path = path.to_str().unwrap();

    path.to_string()
}

#[java(export(class = "com.cyberegylet.antiDupeGallery.backend.Backend", rename = "getQueuedFileProgress"))]
pub extern "C" fn get_queued_file_progress(
    _env: JNIEnv,
    _class: JClass,
) -> jlong {
    let ctx = CONTEXT.get().unwrap().read().unwrap();
    ctx.file_queue_length.load(Ordering::SeqCst) as jlong
}
