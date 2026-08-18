package uz.kmax.compress.domain.repository

interface SettingsRepository {
    suspend fun isFirstLaunch(): Boolean
    suspend fun setFirstLaunchCompleted()
}
