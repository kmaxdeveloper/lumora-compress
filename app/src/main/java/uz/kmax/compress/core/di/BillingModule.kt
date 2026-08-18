package uz.kmax.compress.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.kmax.compress.data.repository.BillingRepositoryImpl
import uz.kmax.compress.domain.repository.BillingRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        billingRepositoryImpl: BillingRepositoryImpl
    ): BillingRepository
}
