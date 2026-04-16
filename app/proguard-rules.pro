# Room entities — keep for R8
-keep class com.app.tabl.data.local.entity.** { *; }
-keep class com.app.tabl.domain.model.** { *; }

# Kotlin serialization
-keepattributes *Annotation*
-dontwarn kotlinx.serialization.**
