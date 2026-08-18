package uz.kmax.compress

import android.app.Application
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LumoraApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var yandexAdsManager: uz.kmax.compress.core.monetization.YandexAdsManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        if (!isMainProcess()) return

        setupStrictMode()
        
        // Initialize Yandex Ads by accessing the injected instance
        yandexAdsManager.hashCode()
    }

    private fun isMainProcess(): Boolean {
        return try {
            val processName = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                getProcessName()
            } else {
                val activityManager = getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                activityManager.runningAppProcesses?.find { it.pid == android.os.Process.myPid() }?.processName
            }
            processName == packageName
        } catch (e: Exception) {
            true
        }
    }

    private fun setupStrictMode() {
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
}
