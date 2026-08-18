package uz.kmax.compress.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.kmax.compress.core.permission.PermissionManager
import uz.kmax.compress.core.permission.PermissionManagerImpl
import uz.kmax.compress.core.storage.FileProviderHelper
import uz.kmax.compress.core.storage.ImagePickerManager
import uz.kmax.compress.core.storage.ImagePickerManagerImpl
import uz.kmax.compress.core.storage.StorageManager
import uz.kmax.compress.core.storage.StorageManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {

    @Provides
    @Singleton
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager = PermissionManagerImpl(context)

    @Provides
    @Singleton
    fun provideStorageManager(
        @ApplicationContext context: Context
    ): StorageManager = StorageManagerImpl(context)

    @Provides
    @Singleton
    fun provideFileProviderHelper(
        @ApplicationContext context: Context
    ): FileProviderHelper = FileProviderHelper(context)

    @Provides
    @Singleton
    fun provideImagePickerManager(): ImagePickerManager = ImagePickerManagerImpl()
}
