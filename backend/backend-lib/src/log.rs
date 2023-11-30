use std::fmt::Display;
use std::sync::Mutex;

use jni::objects::{JClass, JValueGen};
use jni::JNIEnv;

const ANDROID_LOG_CLASS: &'static str = "android/util/Log";
const ANDROID_LOG_FN_SIGNATURE: &'static str =
    "(Ljava/lang/String;Ljava/lang/String;)I";

pub struct Log<'class, 'env: 'class> {
    env: Mutex<JNIEnv<'env>>,
    class: JClass<'class>,
}

impl<'class, 'env: 'class> Log<'env, 'class> {
    pub fn init(mut env: JNIEnv<'env>) -> Self {
        let class = env
            .find_class(ANDROID_LOG_CLASS)
            .expect(&format!("Can't find class `{ANDROID_LOG_CLASS}`"));
        Self {
            env: Mutex::new(env),
            class,
        }
    }

    #[allow(unused)]
    pub fn info(&self, tag: impl Display, msg: impl Display) {
        self.call_logger("i", tag, msg);
    }

    #[allow(unused)]
    pub fn warn(&self, tag: impl Display, msg: impl Display) {
        self.call_logger("w", tag, msg);
    }

    #[allow(unused)]
    pub fn err(&self, tag: impl Display, msg: impl Display) {
        self.call_logger("e", tag, msg);
    }

    fn call_logger(&self, log: &str, tag: impl Display, msg: impl Display) {
        let mut env_lock = self.env.lock().unwrap();
        let tag = env_lock.new_string(tag.to_string()).unwrap();
        let msg = env_lock.new_string(msg.to_string()).unwrap();
        env_lock
            .call_static_method(&self.class, log, ANDROID_LOG_FN_SIGNATURE, &[
                JValueGen::Object(&tag),
                JValueGen::Object(&msg),
            ])
            .unwrap();
    }
}
