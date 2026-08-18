package uz.kmax.compress.core.init

import android.content.Context
import android.os.StrictMode
import androidx.startup.Initializer
import uz.kmax.compress.BuildConfig

class AppInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
