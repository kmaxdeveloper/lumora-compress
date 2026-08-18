package uz.kmax.compress.domain.usecase

import uz.kmax.compress.domain.repository.SettingsRepository
import javax.inject.Inject

class IsFirstLaunchUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Boolean = repository.isFirstLaunch()
}
