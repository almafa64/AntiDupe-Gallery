use backend_proc_macro::java_export;
use jni::objects::JClass;
use jni::sys::{jint, JNIEnv, JNI_VERSION_1_6};
use jni::JavaVM;

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn JNI_OnLoad(_vm: JavaVM, _: *mut ()) -> jint {
    JNI_VERSION_1_6
}

#[java_export(class = "com.cyberegylet.antiDupeGallery.backend.Backend")]
pub extern "C" fn test(_env: JNIEnv, _class: JClass) -> jint {
    546
}
