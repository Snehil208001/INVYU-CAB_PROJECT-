####################################
# FIX: Keep attributes for Generics (CRITICAL)
####################################
# "Signature" alone is not enough. "InnerClasses" and "EnclosingMethod"
# are required for Moshi/Retrofit to detect types in anonymous classes.
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

####################################
# Moshi
####################################
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep class kotlin.Metadata { *; }

# Keep generated adapters if using codegen
-keep class **JsonAdapter { *; }

# If you use KotlinJsonAdapterFactory, you might need this:
-keep class kotlin.reflect.jvm.internal.** { *; }

####################################
# Retrofit & OkHttp
####################################
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Keep generic type information for Retrofit calls
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeVisibleTypeAnnotations

####################################
# Your API response / request models
####################################
# We keep all members (methods/fields) to ensure setters/getters aren't stripped
-keep class com.example.invyucab_project.data.models.** { *; }
-keep class com.example.invyucab_project.domain.model.** { *; }

####################################
# Coroutines
####################################
-keep class kotlinx.coroutines.** { *; }

####################################
# General Protections
####################################
# DO NOT shrink generic superclasses (helps with TypeToken)
-keep class ** extends java.lang.reflect.Type { *; }

# Keep your project classes
-keep class com.example.invyucab_project.** { *; }