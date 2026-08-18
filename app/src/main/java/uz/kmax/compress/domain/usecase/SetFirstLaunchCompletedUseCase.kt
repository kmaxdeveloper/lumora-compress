package uz.kmax.compress.domain.usecase

import uz.kmax.compress.domain.repository.SettingsRepository
import javax.inject.Inject

class SetFirstLaunchCompletedUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() = repository.setFirstLaunchCompleted()
}
