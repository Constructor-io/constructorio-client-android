# Constructor.io Android SDK - Consumer ProGuard Rules
# These rules are bundled with the AAR and applied automatically to consuming apps

####--------Moshi---------####
# Keep annotation/generic-signature metadata that Moshi reads reflectively at runtime.
-keepattributes *Annotation*, Signature, EnclosingMethod, InnerClasses

# Keep Moshi's core runtime classes. R8 (especially full mode) can rename or
# horizontally merge JsonReader/JsonAdapter, which breaks the identity check
# Moshi's AdapterMethodsFactory performs on @FromJson/@ToJson parameter types
# (crash: "Unexpected signature ... fromJson(ya.m, ya.h, ya.h)").
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Keep Moshi annotations so @FromJson/@ToJson reflective adapter discovery works
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *

# Keep Kotlin metadata for Moshi reflection
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

####-----Constructor.io SDK-----####
# Keep SDK model classes (serialization via Moshi)
-keep class io.constructor.data.model.** { *; }

# Keep custom Moshi adapters
-keep class io.constructor.data.model.dataadapter.** { *; }
