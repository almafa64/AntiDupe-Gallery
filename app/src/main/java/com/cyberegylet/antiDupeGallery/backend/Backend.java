package com.cyberegylet.antiDupeGallery.backend;

public class Backend {
    static {
        System.loadLibrary("backend");
    }

    public static native int test();
}
