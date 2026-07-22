# ======================================================================
# MedLog — ProGuard Rules
# ======================================================================

# ── Room Entities ─────────────────────────────────────────────────────
# Keep all entity classes used by Room so that schema serialization
# and reflection-based field access work correctly.

-keep @androidx.room.Entity class * { *; }
-keep class com.medlog.app.data.local.entity.** { *; }

# Keep DAO interfaces (Room uses reflection to implement them)
-keep interface com.medlog.app.data.local.dao.** { *; }

# Keep TypeConverters
-keep class com.medlog.app.data.local.converter.** { *; }

# Keep the database class
-keep class com.medlog.app.data.local.MedLogDatabase { *; }

# ── iText 7 ───────────────────────────────────────────────────────────
# iText uses heavy reflection for its kernel, layout, and IO modules.
# Keep classes that are accessed via Class.forName or reflection.

-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Specifically keep kernel and IO classes that are reflectively loaded
-keepclassmembers class com.itextpdf.kernel.** { *; }
-keepclassmembers class com.itextpdf.io.** { *; }
-keepclassmembers class com.itextpdf.layout.** { *; }

# ── AndroidX ──────────────────────────────────────────────────────────
# Keep Compose runtime classes referenced by Compose compiler
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }

# WorkManager
-keep class androidx.work.** { *; }

# ── Kotlin ────────────────────────────────────────────────────────────
# Keep Kotlin metadata for reflection
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlin serialization (if used in the future)
-dontwarn kotlin.Unit
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# ── General Android ───────────────────────────────────────────────────
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    public *** get*();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ── Model classes ─────────────────────────────────────────────────────
-keep class com.medlog.app.data.model.** { *; }

# ── R class ───────────────────────────────────────────────────────────
-keep class **.R$* {
    *;
}

# ── Remove logging in release builds ─────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ── SLF4J ────────────────────────────────────────────────────────────
# iText pulls in SLF4J API but the impl is not present at runtime
-dontwarn org.slf4j.impl.**
-dontwarn org.slf4j.**