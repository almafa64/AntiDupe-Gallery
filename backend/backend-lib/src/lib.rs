mod log;

use backend_proc_macro::java_export;
use jni::objects::{JClass, JString};
use jni::sys::{jint, JNI_VERSION_1_6};
use jni::{JNIEnv, JavaVM};

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn JNI_OnLoad(_vm: JavaVM, _: *mut ()) -> jint {
    JNI_VERSION_1_6
}

#[java_export(class = "com.cyberegylet.antiDupeGallery.backend.Backend")]
pub extern "C" fn init(mut env: JNIEnv, _class: JClass, work_dir: JString) {
    let work_dir: String = env.get_string(&work_dir).unwrap().into();
}
