####--------Retrofit------####
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions

####--------Okio----------####
-dontwarn okio.**

####--------Moshi---------####
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-keepattributes *Annotation*, Signature, EnclosingMethod, InnerClasses

# Keep Moshi's core runtime classes. R8 (especially full mode) can rename or
# horizontally merge JsonReader/JsonAdapter, which breaks the identity check
# Moshi's AdapterMethodsFactory performs on @FromJson/@ToJson parameter types.
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

####-----Model Classes---####
-keep class io.constructor.data.model.** { *; }

####-----Sample Classes---####
-keep class io.constructor.sample.** { *; }
