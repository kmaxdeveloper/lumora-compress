package uz.kmax.compress.core.compressor

import kotlinx.coroutines.flow.Flow

interface CompressionEngine {
    
    /**
     * Orchestrates the complete compression flow: Pipeline -> OutputWriter -> Metadata.
     */
    fun compress(request: CompressionRequest): Flow<CompressionEngineProgress>
    
    fun cancel(inputUri: android.net.Uri)
    
    fun cancelAll()
}

data class CompressionEngineProgress(
    val stage: CompressionEngineStage,
    val progress: Float = 0f,
    val result: CompressionResult? = null,
    val errorMessage: String? = null
)

enum class CompressionEngineStage {
    PIPELINE_PROCESSING,
    WRITING_FILE,
    APPLYING_METADATA,
    COMPLETED,
    FAILED
}
