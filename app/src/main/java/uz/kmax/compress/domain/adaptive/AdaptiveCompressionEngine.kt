package uz.kmax.compress.domain.adaptive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.kmax.compress.core.compressor.*
import javax.inject.Inject
class AdaptiveCompressionEngine @Inject constructor(private val engine: CompressionEngine, private val calculator: TargetSizeCalculator = TargetSizeCalculator()) {
 fun compress(request:CompressionRequest,target:Long,initialQuality:Int,maxIterations:Int=8): Flow<CompressionLoopResult> = flow { 
     val iterations=mutableListOf<CompressionIteration>()
     var quality=initialQuality
     var best:CompressionResult.Success?=null
     var bestDiff = Long.MAX_VALUE
     
     repeat(maxIterations){ index -> 
         engine.compress(request.copy(quality=CompressionQuality.Custom(quality))).collect { progress -> 
             if(progress.result is CompressionResult.Success){ 
                 val r=progress.result as CompressionResult.Success
                 val diff = kotlin.math.abs(r.compressedSize - target)
                 if (diff < bestDiff) {
                     best = r
                     bestDiff = diff
                 }
                 iterations+=CompressionIteration(index+1,quality,r.compressedSize)
                 val reached=diff.toFloat()/target<=.03f
                 emit(CompressionLoopResult(best!!, target, iterations.toList(), reached))
                 if(!reached) quality=calculator.nextQuality(quality,r.compressedSize,target) 
             } 
         } 
         if(best!=null && kotlin.math.abs(best!!.compressedSize-target).toFloat()/target<=.03f) return@flow 
     } 
 }
}
