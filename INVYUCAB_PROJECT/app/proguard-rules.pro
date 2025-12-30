####################################
# Keep annotations (VERY IMPORTANT)
####################################
-keepattributes Annotation

####################################
# Moshi
####################################
-keep class com.squareup.moshi.** { *; }
-keep class kotlin.Metadata { *; }

# If using @JsonClass(generateAdapter = true)
-keep class **JsonAdapter { *; }

####################################
# Retrofit & OkHttp
####################################
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

####################################
# Your API response / request models
####################################
-keep class com.example.invyucab_project.data.models.** { *; }
-keep class com.example.invyucab_project.domain.model.** { *; }


####################################
# Coroutines (safe)
####################################
-keep class kotlinx.coroutines.** { *; }

-keepattributes Signature

# DO NOT shrink generic superclasses
-keep class ** extends java.lang.reflect.Type { *; }

# Keep ALL sealed / generic wrappers
-keep class com.example.invyucab_project.** { *; }

