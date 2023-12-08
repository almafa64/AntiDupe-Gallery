mod exports;

use std::panic;

use jni::sys::{jint, JNI_VERSION_1_6};
use jni::JavaVM;

use crate::log;

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn JNI_OnLoad(vm: JavaVM, _: *mut ()) -> jint {
    panic::set_hook(Box::new(move |info| {
        let mut env = vm.get_env().expect("Can't get JNI env");
        if let Some(&msg) = info.payload().downcast_ref::<&str>() {
            log::err(&mut env, "Backend panic", msg);
        } else if let Some(msg) = info.payload().downcast_ref::<String>() {
            log::err(&mut env, "Backend panic", msg);
        } else if let Some(msg) = info.payload().downcast_ref::<anyhow::Error>()
        {
            log::err(&mut env, "Backend panic", msg);
        }
    }));
    JNI_VERSION_1_6
}
