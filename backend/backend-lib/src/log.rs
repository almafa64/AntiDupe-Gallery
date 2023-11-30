use std::fmt::Display;
use std::sync::Mutex;

use jni::objects::{JClass, JValueGen};
use jni::JNIEnv;

const ANDROID_LOG_CLASS: &'static str = "android/util/Log";
const ANDROID_LOG_FN_SIGNATURE: &'static str =
    "(Ljava/lang/String;Ljava/lang/String;)I";

#[allow(unused)]
pub fn debug(env: &mut JNIEnv, tag: impl Display, msg: impl Display) {
    call_logger_fn(env, "d", tag, msg);
}

#[allow(unused)]
pub fn info(env: &mut JNIEnv, tag: impl Display, msg: impl Display) {
    call_logger_fn(env, "i", tag, msg);
}

#[allow(unused)]
pub fn warn(env: &mut JNIEnv, tag: impl Display, msg: impl Display) {
    call_logger_fn(env, "w", tag, msg);
}

#[allow(unused)]
pub fn err(env: &mut JNIEnv, tag: impl Display, msg: impl Display) {
    call_logger_fn(env, "e", tag, msg);
}

fn call_logger_fn(
    env: &mut JNIEnv,
    name: &str,
    tag: impl Display,
    msg: impl Display,
) {
    let tag = env.new_string(tag.to_string()).unwrap();
    let msg = env.new_string(msg.to_string()).unwrap();
    env.call_static_method(
        ANDROID_LOG_CLASS,
        name,
        ANDROID_LOG_FN_SIGNATURE,
        &[JValueGen::Object(&tag), JValueGen::Object(&msg)],
    )
    .unwrap();
}
