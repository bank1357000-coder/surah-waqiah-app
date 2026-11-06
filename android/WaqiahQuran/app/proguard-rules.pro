# kotlinx.serialization — keep generated serializers for asset/route models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.alwaqiah.quran.**$$serializer { *; }
-keepclassmembers class com.alwaqiah.quran.** { *** Companion; }
-keepclasseswithmembers class com.alwaqiah.quran.** { kotlinx.serialization.KSerializer serializer(...); }
