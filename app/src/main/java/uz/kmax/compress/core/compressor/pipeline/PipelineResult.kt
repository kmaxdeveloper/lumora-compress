package uz.kmax.compress.core.compressor.pipeline

import uz.kmax.compress.core.compressor.CompressionResult

data class PipelineResult(
    val result: CompressionResult,
    val totalTime: Long
)
