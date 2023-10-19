use jni::{
    objects::JClass,
    sys::{jint, JNI_VERSION_1_6},
    JNIEnv, JavaVM, NativeMethod,
};

const CLASSNAME: &'static str = "com/cyberegylet/antiDupeGallery/backend/Test";

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn JNI_OnLoad(vm: JavaVM, _: *mut ()) -> jint {
    let mut env = vm.get_env().expect("failed to get vm env");

    let class = env
        .find_class(CLASSNAME)
        .expect(&format!("cannot find class {CLASSNAME}"));

    let native_methods = vec![NativeMethod {
        name: "test".into(),
        sig: "()I".into(),
        fn_ptr: test as _,
    }];

    env.register_native_methods(class, &native_methods)
        .expect("failed to register native methods");

    JNI_VERSION_1_6
}

fn test(_env: JNIEnv, _class: JClass) -> jint {
    56727
}
