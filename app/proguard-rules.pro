# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class app.worthit.**$$serializer { *; }
-keepclassmembers class app.worthit.** { *** Companion; }
-keepclasseswithmembers class app.worthit.** { kotlinx.serialization.KSerializer serializer(...); }
-keep class app.worthit.data.** { *; }
