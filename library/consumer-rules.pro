# Constructor.io Android SDK - Consumer ProGuard Rules
# These rules are bundled with the AAR and applied automatically to consuming apps

####--------Moshi---------####
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
