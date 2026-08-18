package uz.kmax.compress.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.kmax.compress.R
import uz.kmax.compress.core.analytics.AnalyticsManager
import uz.kmax.compress.core.compressor.CompressionEngine
import uz.kmax.compress.core.compressor.CompressionEngineStage
import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.CompressionQuality
import uz.kmax.compress.core.compressor.CompressionRequest
import uz.kmax.compress.core.compressor.CompressionResult
import uz.kmax.compress.core.compressor.metadata.MetadataOptions
import uz.kmax.compress.core.storage.SelectedImagesRepository
import uz.kmax.compress.core.storage.StorageManager
import uz.kmax.compress.domain.adaptive.CompressionLoopResult
import uz.kmax.compress.domain.usecase.CompressionLoopUseCase
import uz.kmax.compress.domain.usecase.InsertHistoryUseCase
import uz.kmax.compress.domain.model.CompressionHistory
import uz.kmax.compress.feature.batch.model.BatchItemUiModel
import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel
import java.io.File
import java.util.UUID
import javax.inject.Inject

sealed class BatchState {
    object Idle : BatchState()
    data class Processing(val total: Int, val completed: Int, val currentItem: BatchItemUiModel? = null, val lastUpdate: BatchItemUpdate? = null) : BatchState()
    data class Finished(val success: Int, val failed: Int) : BatchState()
    data class Failed(val message: String) : BatchState()
}

data class BatchItemUpdate(val id: String, val status: BatchItemUiModel.Status, val progress: Int, val originalSize: Long = 0, val compressedSize: Long = 0, val outputUri: Uri? = null)

sealed class SingleCompressionState {
    object Idle : SingleCompressionState()
    data class Processing(val requestId: String, val progress: Int, val iteration: Int = 0) : SingleCompressionState()
    data class Completed(val requestId: String, val result: CompressionResult.Success, val targetResult: CompressionLoopResult? = null) : SingleCompressionState()
    data class Failed(val requestId: String, val message: String) : SingleCompressionState()
}

/** The sole executor for user-requested compression. Intent extras contain only scalar data/URIs. */
@AndroidEntryPoint
class CompressionService : Service() {
    @Inject lateinit var engine: CompressionEngine
    @Inject lateinit var compressionLoopUseCase: CompressionLoopUseCase
    @Inject lateinit var storageManager: StorageManager
    @Inject lateinit var selectedImagesRepository: SelectedImagesRepository
    @Inject lateinit var insertHistoryUseCase: InsertHistoryUseCase
    @Inject lateinit var analyticsManager: AnalyticsManager

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var compressionJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "compression_channel"
        private const val NOTIFICATION_ID = 1001
        private const val EXTRA_INPUT_URI = "extra_input_uri"
        private const val EXTRA_OUTPUT_URI = "extra_output_uri"
        private const val EXTRA_FORMAT = "extra_format"
        private const val EXTRA_QUALITY = "extra_quality"
        private const val EXTRA_KEEP_METADATA = "extra_keep_metadata"
        private const val EXTRA_METADATA_STRATEGY = "extra_metadata_strategy"
        private const val EXTRA_RESIZE_WIDTH = "extra_resize_width"
        private const val EXTRA_RESIZE_HEIGHT = "extra_resize_height"
        private const val EXTRA_TARGET_BYTES = "extra_target_bytes"
        private const val EXTRA_REQUEST_ID = "extra_request_id"
        private const val EXTRA_BATCH_ID = "extra_batch_id"
        private const val EXTRA_IS_BATCH = "extra_is_batch"
        private const val ACTION_STOP = "action_stop"

        private val _batchState = MutableStateFlow<BatchState>(BatchState.Idle)
        val batchState: StateFlow<BatchState> = _batchState.asStateFlow()
        private val _singleState = MutableStateFlow<SingleCompressionState>(SingleCompressionState.Idle)
        val singleState: StateFlow<SingleCompressionState> = _singleState.asStateFlow()

        fun start(context: Context, request: CompressionRequest, targetBytes: Long? = null): String {
            val requestId = UUID.randomUUID().toString()
            val intent = Intent(context, CompressionService::class.java).apply {
                putExtra(EXTRA_INPUT_URI, request.inputUri.toString())
                putExtra(EXTRA_OUTPUT_URI, request.outputUri.toString())
                putExtra(EXTRA_FORMAT, request.format.name)
                putExtra(EXTRA_QUALITY, request.quality.value)
                putExtra(EXTRA_KEEP_METADATA, request.keepMetadata)
                putExtra(EXTRA_METADATA_STRATEGY, request.metadataOptions.strategy.name)
                putExtra(EXTRA_RESIZE_WIDTH, request.resizeOptions?.width ?: -1)
                putExtra(EXTRA_RESIZE_HEIGHT, request.resizeOptions?.height ?: -1)
                putExtra(EXTRA_TARGET_BYTES, targetBytes ?: -1L)
                putExtra(EXTRA_REQUEST_ID, requestId)
                putExtra(EXTRA_IS_BATCH, false)
            }
            startService(context, intent)
            return requestId
        }

        fun startBatch(context: Context, batchId: String) {
            startService(context, Intent(context, CompressionService::class.java).apply {
                putExtra(EXTRA_IS_BATCH, true)
                putExtra(EXTRA_BATCH_ID, batchId)
            })
        }

        fun stop(context: Context) {
            context.startService(Intent(context, CompressionService::class.java).setAction(ACTION_STOP))
        }

        private fun startService(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
        }
    }

    override fun onCreate() { super.onCreate(); createNotificationChannel() }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            compressionJob?.cancel()
            stopForegroundCompat(remove = true)
            stopSelf()
            return START_NOT_STICKY
        }
        
        // If we are already processing something, don't start another job from a new intent.
        // The current job is authoritative.
        if (compressionJob?.isActive == true) return START_REDELIVER_INTENT

        if (intent?.getBooleanExtra(EXTRA_IS_BATCH, false) == true) {
            val batchId = intent.getStringExtra(EXTRA_BATCH_ID)
            val images = batchId?.let(selectedImagesRepository::peek)
            if (images.isNullOrEmpty()) {
                _batchState.value = BatchState.Failed("Batch is no longer available. Please select images again.")
                stopForegroundCompat(remove = true)
                stopSelf(startId)
                return START_NOT_STICKY
            }
            startForegroundCompat("Preparing batch compression", 0)
            startBatchProcessing(images, batchId)
            return START_REDELIVER_INTENT
        }

        val requestId = intent?.getStringExtra(EXTRA_REQUEST_ID)
        val request = intent?.toRequest()
        if (requestId == null || request == null) {
            if (requestId != null) _singleState.value = SingleCompressionState.Failed(requestId, "Invalid compression request")
            stopForegroundCompat(remove = true)
            stopSelf(startId)
            return START_NOT_STICKY
        }
        startForegroundCompat("Preparing compression", 0)
        startSingleProcessing(requestId, request, intent.getLongExtra(EXTRA_TARGET_BYTES, -1L).takeIf { it > 0L })
        return START_REDELIVER_INTENT
    }

    private fun Intent.toRequest(): CompressionRequest? = runCatching {
        val input = getStringExtra(EXTRA_INPUT_URI)?.let(Uri::parse) ?: return null
        val output = getStringExtra(EXTRA_OUTPUT_URI)?.let(Uri::parse) ?: return null
        val format = CompressionFormat.valueOf(getStringExtra(EXTRA_FORMAT) ?: return null)
        val strategy = MetadataOptions.Strategy.valueOf(getStringExtra(EXTRA_METADATA_STRATEGY) ?: MetadataOptions.Strategy.REMOVE_ALL.name)
        val width = getIntExtra(EXTRA_RESIZE_WIDTH, -1)
        val height = getIntExtra(EXTRA_RESIZE_HEIGHT, -1)
        CompressionRequest(input, output, format, CompressionQuality.Custom(getIntExtra(EXTRA_QUALITY, 80)), getBooleanExtra(EXTRA_KEEP_METADATA, false), MetadataOptions(strategy), if (width > 0 && height > 0) CompressionRequest.ResizeOptions(width, height) else null)
    }.getOrNull()

    private fun startSingleProcessing(requestId: String, request: CompressionRequest, targetBytes: Long?) {
        compressionJob = serviceScope.launch {
            try {
                if (targetBytes != null) {
                    var final: CompressionLoopResult? = null
                    compressionLoopUseCase(request, targetBytes, request.quality.value).collect { loop ->
                        final = loop
                        _singleState.value = SingleCompressionState.Processing(requestId, 0, loop.iterations.size)
                    }
                    val result = final?.result
                    if (result != null) {
                        saveToHistory(request.inputUri.lastPathSegment ?: "image", request.inputUri, result)
                        _singleState.value = SingleCompressionState.Completed(requestId, result, final)
                    }
                    else _singleState.value = SingleCompressionState.Failed(requestId, "Unable to reach target size")
                } else {
                    engine.compress(request).collect { progress ->
                        _singleState.value = when (progress.stage) {
                            CompressionEngineStage.COMPLETED -> {
                                val res = progress.result as CompressionResult.Success
                                saveToHistory(request.inputUri.lastPathSegment ?: "image", request.inputUri, res)
                                analyticsManager.logCompressFinished(res.savedPercent, res.savedBytes)
                                SingleCompressionState.Completed(requestId, res)
                            }
                            CompressionEngineStage.FAILED -> {
                                val msg = progress.errorMessage ?: "Compression failed"
                                analyticsManager.logCompressFailed(msg)
                                SingleCompressionState.Failed(requestId, msg)
                            }
                            else -> SingleCompressionState.Processing(requestId, (progress.progress * 100).toInt())
                        }
                        if (progress.stage != CompressionEngineStage.COMPLETED && progress.stage != CompressionEngineStage.FAILED) startForegroundCompat("Compressing image… ${(progress.progress * 100).toInt()}%", (progress.progress * 100).toInt())
                    }
                }
            } catch (e: Exception) {
                _singleState.value = SingleCompressionState.Failed(requestId, e.message ?: "Compression failed")
            } finally {
                stopForegroundCompat(remove = true)
                stopSelf()
            }
        }
    }

    private fun startBatchProcessing(images: List<GalleryImageUiModel>, batchId: String) {
        compressionJob = serviceScope.launch {
            var success = 0; var failed = 0
            _batchState.value = BatchState.Processing(images.size, 0)
            try {
                images.forEachIndexed { index, model ->
                    val item = BatchItemUiModel(model.uri.toString(), model.uri, model.name)
                    _batchState.value = BatchState.Processing(images.size, success + failed, item, BatchItemUpdate(item.id, BatchItemUiModel.Status.PROCESSING, 0))
                    engine.compress(CompressionRequest(model.uri, Uri.fromFile(storageManager.createTempFile("jpg", model.name)))).collect { progress ->
                        when (progress.stage) {
                            CompressionEngineStage.COMPLETED -> {
                                val result = progress.result as? CompressionResult.Success ?: return@collect
                                success++
                                saveToGallery(model.name, result)
                                saveToHistory(model.name, model.uri, result)
                                _batchState.value = BatchState.Processing(images.size, success + failed, item, BatchItemUpdate(item.id, BatchItemUiModel.Status.COMPLETED, 100, result.originalSize, result.compressedSize, result.outputUri))
                            }
                            CompressionEngineStage.FAILED -> {
                                failed++
                                _batchState.value = BatchState.Processing(images.size, success + failed, item, BatchItemUpdate(item.id, BatchItemUiModel.Status.FAILED, 0))
                            }
                            else -> {
                                val percent = (progress.progress * 100).toInt()
                                _batchState.value = BatchState.Processing(images.size, success + failed, item, BatchItemUpdate(item.id, BatchItemUiModel.Status.PROCESSING, percent))
                                startForegroundCompat("Batch: ${success + failed}/${images.size}", ((index * 100 + percent) / images.size))
                            }
                        }
                    }
                }
                _batchState.value = BatchState.Finished(success, failed)
                selectedImagesRepository.claim(batchId) // Safely remove after completion
            } catch (e: Exception) {
                _batchState.value = BatchState.Failed(e.message ?: "Batch compression failed")
            } finally {
                stopForegroundCompat(remove = true)
                stopSelf()
            }
        }
    }

    private fun saveToGallery(originalName: String, result: CompressionResult.Success) = runCatching {
        val file = storageManager.uriToFile(result.outputUri) ?: return@runCatching
        val extension = result.outputUri.lastPathSegment?.substringAfterLast('.', "jpg") ?: "jpg"
        storageManager.saveToPublicStorage(file, if (extension == "png") "image/png" else if (extension == "webp") "image/webp" else "image/jpeg", "Lumora_Batch_${originalName.substringBeforeLast('.')}.${extension}")
    }

    private fun saveToHistory(name: String, input: Uri, result: CompressionResult.Success) {
        serviceScope.launch {
            insertHistoryUseCase(
                CompressionHistory(
                    originalUri = input.toString(),
                    compressedUri = result.outputUri.toString(),
                    originalSize = result.originalSize,
                    compressedSize = result.compressedSize,
                    savedBytes = result.savedBytes,
                    savedPercent = result.savedPercent,
                    format = result.outputUri.lastPathSegment?.substringAfterLast('.')?.uppercase() ?: "JPG",
                    resolution = "${result.statistics.outputResolution.first}x${result.statistics.outputResolution.second}",
                    metadataMode = "REMOVE_ALL",
                    quality = 80,
                    createdAt = System.currentTimeMillis(),
                    favorite = false
                )
            )
        }
    }

    private fun startForegroundCompat(message: String, progress: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Lumora Compress").setContentText(message).setSmallIcon(R.drawable.ic_launcher_foreground).setPriority(NotificationCompat.PRIORITY_LOW).setOngoing(true).setProgress(100, progress.coerceIn(0, 100), false).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC) else startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopForegroundCompat(remove: Boolean) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(if (remove) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH) else @Suppress("DEPRECATION") stopForeground(remove) }
    private fun createNotificationChannel() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL_ID, "Image Compression", NotificationManager.IMPORTANCE_LOW)) }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { compressionJob?.cancel(); serviceScope.cancel(); super.onDestroy() }
}
