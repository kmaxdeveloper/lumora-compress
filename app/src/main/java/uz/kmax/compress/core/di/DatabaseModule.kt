package uz.kmax.compress.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.kmax.compress.data.local.AppDatabase
import uz.kmax.compress.data.local.dao.HistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lumora_compress.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }
}
