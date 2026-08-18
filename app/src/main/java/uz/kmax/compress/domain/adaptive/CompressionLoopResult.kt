package uz.kmax.compress.domain.adaptive
import uz.kmax.compress.core.compressor.CompressionResult
data class CompressionLoopResult(val result: CompressionResult.Success?, val targetBytes:Long, val iterations:List<CompressionIteration>, val reachedTarget:Boolean)
