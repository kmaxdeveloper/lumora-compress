package uz.kmax.compress.core.compressor.impl

import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import uz.kmax.compress.core.analytics.CrashlyticsManager
import uz.kmax.compress.core.compressor.*
import uz.kmax.compress.core.compressor.metadata.MetadataOptions
import uz.kmax.compress.core.compressor.metadata.MetadataProcessor
import uz.kmax.compress.core.compressor.pipeline.CompressionPipeline
import uz.kmax.compress.core.compressor.pipeline.CompressionStage
import uz.kmax.compress.core.compressor.writer.OutputDestination
import uz.kmax.compress.core.compressor.writer.OutputRequest
import uz.kmax.compress.core.compressor.writer.OutputWriter
import uz.kmax.compress.core.di.qualifier.IoDispatcher
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompressionEngineImpl @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val pipeline: CompressionPipeline,
    private val writer: OutputWriter,
    private val metadataProcessor: MetadataProcessor,
    private val crashlyticsManager: CrashlyticsManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CompressionEngine {

    private val activeJobs = ConcurrentHashMap<Uri, Job>()

    override fun compress(request: CompressionRequest): Flow<CompressionEngineProgress> = flow {
        val startTime = System.currentTimeMillis()
        val originalSize = getFileSize(request.inputUri)
        val origRes = getImageResolution(request.inputUri)

        try {
            var compressedData: uz.kmax.compress.core.compressor.algorithm.CompressedImageData? = null

            pipeline.execute(request).collect { progress ->
                when (progress.stage) {
                    CompressionStage.FAILED -> throw Exception(progress.message ?: "Pipeline failed")
                    CompressionStage.FINISHED -> {
                        compressedData = progress.imageData
                    }
                    else -> {
                        emit(CompressionEngineProgress(CompressionEngineStage.PIPELINE_PROCESSING, progress.progress * 0.7f))
                    }
                }
            }

            val data = compressedData ?: throw Exception("Pipeline produced no data")

            emit(CompressionEngineProgress(CompressionEngineStage.WRITING_FILE, 0.8f))
            val outputResult = try {
                writer.write(
                    OutputRequest(
                        data = data.data,
                        mimeType = data.format.mimeType,
                        fileName = outputFileName(request.outputUri.lastPathSegment, data.format),
                        destination = if (request.outputUri.scheme == "file") {
                            request.outputUri.path?.let(::File)?.let { outputFile ->
                                File(outputFile.parentFile ?: context.cacheDir, outputFileName(outputFile.name, data.format))
                            }?.let(OutputDestination::CustomFile)
                                ?: OutputDestination.Cache
                        } else {
                            OutputDestination.Cache
                        },
                        overwrite = request.overwriteExistingFile
                    )
                )
            } catch (e: Exception) {
                crashlyticsManager.recordException(e)
                throw e
            }

            emit(CompressionEngineProgress(CompressionEngineStage.APPLYING_METADATA, 0.9f))
            try {
                val outputPath = outputResult.absolutePath
                if (!outputPath.isNullOrEmpty()) {
                    metadataProcessor.process(
                        request.inputUri,
                        File(outputPath),
                        request.metadataOptions
                    )
                }
            } catch (e: Exception) {
                crashlyticsManager.recordException(e)
            }

            val elapsedTime = System.currentTimeMillis() - startTime
            val outputRes = getImageResolution(outputResult.uri)
            val ratio = if (originalSize > 0) outputResult.fileSize.toFloat() / originalSize.toFloat() else 1.0f

            finalResult(originalSize, elapsedTime, outputResult, origRes, outputRes, ratio).also { result ->
                emit(CompressionEngineProgress(CompressionEngineStage.COMPLETED, 1.0f, result))
            }

        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            // Cleanup on failure
            try {
                if (request.outputUri.scheme == "file") {
                    request.outputUri.path?.let(::File)?.let { file ->
                        if (file.exists()) file.delete()
                    }
                }
            } catch (cleanupException: Exception) {
                // Ignore cleanup errors
            }
            emit(CompressionEngineProgress(CompressionEngineStage.FAILED, errorMessage = e.message ?: "Unknown compression error"))
        } finally {
            activeJobs.remove(request.inputUri)
        }
    }.flowOn(ioDispatcher)

    private fun finalResult(
        originalSize: Long,
        elapsedTime: Long,
        outputResult: uz.kmax.compress.core.compressor.writer.OutputResult,
        origRes: Pair<Int, Int>,
        outputRes: Pair<Int, Int>,
        ratio: Float
    ): CompressionResult.Success {
        val savedBytes = originalSize - outputResult.fileSize
        val savedPercent = if (originalSize > 0) (savedBytes.toFloat() / originalSize.toFloat()) * 100f else 0f
        return CompressionResult.Success(
            originalSize = originalSize,
            elapsedTime = elapsedTime,
            compressedSize = outputResult.fileSize,
            savedBytes = savedBytes,
            savedPercent = savedPercent,
            outputUri = outputResult.uri,
            statistics = CompressionStatistics(
                compressionRatio = ratio,
                processingTimeMs = elapsedTime,
                originalResolution = origRes,
                outputResolution = outputRes
            )
        )
    }

    private fun getImageResolution(uri: Uri): Pair<Int, Int> {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            
            val rotation = try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    androidx.exifinterface.media.ExifInterface(input).getAttributeInt(
                        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
            } catch (e: Exception) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
            }

            if (rotation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 || 
                rotation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270) {
                options.outHeight to options.outWidth
            } else {
                options.outWidth to options.outHeight
            }
        } catch (e: Exception) {
            0 to 0
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { 
                it.length 
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun outputFileName(sourceName: String?, format: CompressionFormat): String {
        val name = sourceName?.substringBeforeLast('.')
            ?.ifBlank { null }
            ?: "compressed_image_${System.currentTimeMillis()}"
        return "$name.${format.fileExtension}"
    }

    override fun cancel(inputUri: Uri) {
        activeJobs.remove(inputUri)?.cancel()
    }

    override fun cancelAll() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
    }
}
