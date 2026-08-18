package uz.kmax.compress.core.compressor.pipeline

enum class CompressionStage {
    INITIALIZING,
    DECODING,
    ROTATING,
    RESIZING,
    ENCODING,
    WRITING,
    METADATA,
    FINISHED,
    FAILED
}
