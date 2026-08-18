# Hilt rules
-keep,allowobfuscation,allowoptimization @dagger.hilt.android.lifecycle.HiltViewModel class *
-keep,allowobfuscation,allowoptimization @dagger.hilt.EntryPoint class *

# Room rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * { @androidx.room.PrimaryKey *; }

# Paging rules
-keep class androidx.paging.PagingSource { *; }

# Coil rules
-keep class coil.** { *; }
-dontwarn coil.**

# DataStore rules
-keep class androidx.datastore.** { *; }

# Navigation rules
-keep class * extends androidx.navigation.NavArgs

# Parcelize rules
-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Lumora Specific
-keep class uz.kmax.compress.data.local.entity.** { *; }
-keep class uz.kmax.compress.feature.gallery.model.** { *; }
-keep class uz.kmax.compress.feature.compare.model.** { *; }

# Yandex Mobile Ads
-keep class com.yandex.mobileads.** { *; }
-keep class com.yandex.metrica.** { *; }
-dontwarn com.yandex.metrica.**
-dontwarn com.yandex.varioqub.**
-dontwarn com.yandex.mobileads.**
