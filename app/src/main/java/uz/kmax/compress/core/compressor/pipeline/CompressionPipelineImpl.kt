package uz.kmax.compress.core.compressor.pipeline

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import uz.kmax.compress.core.analytics.CrashlyticsManager
import uz.kmax.compress.core.compressor.CompressionException
import uz.kmax.compress.core.compressor.CompressionRequest
import uz.kmax.compress.core.compressor.processor.OrientationProcessor
import uz.kmax.compress.core.compressor.processor.ResizeProcessor
import uz.kmax.compress.core.compressor.processor.ResizeOptions
import uz.kmax.compress.core.compressor.processor.ResizeStrategy
import uz.kmax.compress.core.compressor.algorithm.CompressionAlgorithmFactory
import uz.kmax.compress.core.compressor.decoder.BitmapDecoder
import uz.kmax.compress.core.di.qualifier.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompressionPipelineImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val decoder: BitmapDecoder,
    private val orientationProcessor: OrientationProcessor,
    private val resizeProcessor: ResizeProcessor,
    private val algorithmFactory: CompressionAlgorithmFactory,
    private val crashlyticsManager: CrashlyticsManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CompressionPipeline {

    override fun execute(request: CompressionRequest): Flow<CompressionProgress> = flow {
        val pipelineContext = PipelineContext(startTime = System.currentTimeMillis())
        
        try {
            emit(CompressionProgress(CompressionStage.INITIALIZING))

            // 1. Decode
            emit(CompressionProgress(CompressionStage.DECODING, 0.2f))
            pipelineContext.decodedBitmap = decoder.decode(request.inputUri)

            // 2. Orientation
            emit(CompressionProgress(CompressionStage.ROTATING, 0.4f))
            pipelineContext.rotatedBitmap = orientationProcessor.process(pipelineContext.decodedBitmap!!)

            // 3. Resize
            emit(CompressionProgress(CompressionStage.RESIZING, 0.6f))
            val resizeResult = resizeProcessor.process(
                pipelineContext.rotatedBitmap!!,
                request.resizeOptions?.let { 
                    ResizeOptions(it.width, it.height, ResizeStrategy.KEEP_ASPECT_RATIO) 
                } ?: ResizeOptions()
            )
            pipelineContext.resizedBitmap = resizeResult.bitmap

            // 4. Algorithm & Encode
            emit(CompressionProgress(CompressionStage.ENCODING, 0.8f))
            val algorithm = algorithmFactory.getAlgorithm(pipelineContext.resizedBitmap!!, request)
            val imageData = algorithm.compress(pipelineContext.resizedBitmap!!, request)
            pipelineContext.compressedImageData = imageData

            emit(CompressionProgress(CompressionStage.FINISHED, 1.0f, imageData = imageData))

        } catch (e: Exception) {
            val exception = if (e is CompressionException) e else CompressionException.Unknown(e)
            crashlyticsManager.recordCompressionError(exception, request.format.name, request.quality.value)
            emit(CompressionProgress(CompressionStage.FAILED, message = exception.message))
        } finally {
            recycleBitmaps(pipelineContext)
        }
    }.flowOn(ioDispatcher)

    private fun recycleBitmaps(context: PipelineContext) {
        try {
            val decoded = context.decodedBitmap?.bitmap
            val rotated = context.rotatedBitmap
            val resized = context.resizedBitmap

            if (decoded != null && !decoded.isRecycled) {
                if (decoded !== rotated && decoded !== resized) {
                    decoded.recycle()
                }
            }
            
            if (rotated != null && !rotated.isRecycled) {
                if (rotated !== resized) {
                    rotated.recycle()
                }
            }
            
            if (resized != null && !resized.isRecycled) {
                resized.recycle()
            }
        } catch (e: Exception) {
            // Silently catch recycling errors
        }
    }
}
