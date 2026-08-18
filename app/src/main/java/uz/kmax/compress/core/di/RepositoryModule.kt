package uz.kmax.compress.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.kmax.compress.data.repository.HistoryRepositoryImpl
import uz.kmax.compress.data.repository.SettingsRepositoryImpl
import uz.kmax.compress.data.repository.StorageGuardianRepositoryImpl
import uz.kmax.compress.domain.repository.HistoryRepository
import uz.kmax.compress.domain.repository.SettingsRepository
import uz.kmax.compress.domain.repository.StorageGuardianRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        historyRepositoryImpl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindStorageGuardianRepository(
        storageGuardianRepositoryImpl: StorageGuardianRepositoryImpl
    ): StorageGuardianRepository
}
