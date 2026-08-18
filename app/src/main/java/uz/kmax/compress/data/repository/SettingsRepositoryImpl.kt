package uz.kmax.compress.data.repository

import kotlinx.coroutines.flow.first
import uz.kmax.compress.core.preferences.PreferencesManager
import uz.kmax.compress.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : SettingsRepository {

//    override suspend fun isFirstLaunch(): Boolean {
//        return preferencesManager.isFirstLaunch.first()
//    }

    override suspend fun setFirstLaunchCompleted() {
        preferencesManager.setFirstLaunchCompleted()
    }

    override suspend fun isFirstLaunch(): Boolean {
        return preferencesManager.preferencesFlow
            .first()
            .isFirstLaunch
    }
}
