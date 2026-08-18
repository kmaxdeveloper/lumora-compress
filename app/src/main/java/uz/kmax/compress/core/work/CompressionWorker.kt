package uz.kmax.compress.core.work

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import uz.kmax.compress.core.compressor.*

@HiltWorker
class CompressionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val engine: CompressionEngine
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val inputUriString = inputData.getString(KEY_INPUT_URI) ?: return Result.failure()
        val formatName = inputData.getString(KEY_FORMAT) ?: CompressionFormat.JPEG.name
        val quality = inputData.getInt(KEY_QUALITY, 80)
        val keepMetadata = inputData.getBoolean(KEY_KEEP_METADATA, true)

        val inputUri = Uri.parse(inputUriString)
        val format = try { CompressionFormat.valueOf(formatName) } catch (e: Exception) { CompressionFormat.JPEG }

        val request = CompressionRequest(
            inputUri = inputUri,
            outputUri = Uri.fromFile(File(applicationContext.cacheDir, "worker_${System.currentTimeMillis()}.${format.fileExtension}")),
            format = format,
            quality = CompressionQuality.Custom(quality),
            keepMetadata = keepMetadata
        )

        return try {
            var isSuccess = false
            engine.compress(request).collect { progress ->
                setProgress(workDataOf(KEY_PROGRESS to (progress.progress * 100).toInt()))
                if (progress.stage == CompressionEngineStage.COMPLETED) {
                    isSuccess = true
                }
            }
            if (isSuccess) Result.success() else Result.failure()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val KEY_INPUT_URI = "key_input_uri"
        const val KEY_FORMAT = "key_format"
        const val KEY_QUALITY = "key_quality"
        const val KEY_KEEP_METADATA = "key_keep_metadata"
        const val KEY_PROGRESS = "key_progress"
    }
}
