package uz.kmax.compress.domain.adaptive
import kotlin.math.abs
import javax.inject.Inject
class TargetSizeCalculator @Inject constructor() { fun nextQuality(current:Int, size:Long, target:Long):Int = (current * target.toDouble() / size.coerceAtLeast(1)).toInt().coerceIn(15, 100); fun accuracy(size:Long,target:Long)= (100f - abs(size-target).toFloat()/target*100).coerceIn(0f,100f) }
