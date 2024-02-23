# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# only for debugging release
-dontobfuscate
-keepattributes SourceFile,LineNumberTable

-keep public class com.cyberegylet.antiDupeGallery.activities.ImageListBaseActivity {
    protected java.lang.String getDbPath(java.lang.String);
}
-keep public class com.cyberegylet.antiDupeGallery.backend.Backend { *; }
-keep public class com.cyberegylet.antiDupeGallery.backend.Backend$* { *; }
