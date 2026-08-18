package uz.kmax.compress.core.compressor.pipeline

import kotlinx.coroutines.flow.Flow
import uz.kmax.compress.core.compressor.CompressionRequest
import uz.kmax.compress.core.compressor.CompressionResult

interface CompressionPipeline {
    /**
     * Executes the compression pipeline for a single request.
     *
     * @param request The compression request details.
     * @return A Flow of progress updates, terminating with the final result.
     */
    fun execute(request: CompressionRequest): Flow<CompressionProgress>
}
