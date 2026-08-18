package uz.kmax.compress.domain.usecase

import android.net.Uri
import uz.kmax.compress.core.social.SocialOptimizer
import uz.kmax.compress.core.social.SocialPreset
import uz.kmax.compress.core.smart.SmartCompressionDecision
import javax.inject.Inject

class CreateSocialCompressionRequestUseCase @Inject constructor(private val optimizer: SocialOptimizer) {
    operator fun invoke(input: Uri, output: Uri, preset: SocialPreset, smart: SmartCompressionDecision?) = optimizer.optimize(input, output, preset, smart)
}
