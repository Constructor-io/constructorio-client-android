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
