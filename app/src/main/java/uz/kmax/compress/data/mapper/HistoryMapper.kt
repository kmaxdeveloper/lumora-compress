package uz.kmax.compress.data.mapper

import uz.kmax.compress.data.local.entity.CompressionHistoryEntity
import uz.kmax.compress.domain.model.CompressionHistory

fun CompressionHistoryEntity.toDomain() = CompressionHistory(
    id = id,
    originalUri = originalUri,
    compressedUri = compressedUri,
    originalSize = originalSize,
    compressedSize = compressedSize,
    savedBytes = savedBytes,
    savedPercent = savedPercent,
    format = format,
    resolution = resolution,
    createdAt = createdAt,
    metadataMode = metadataMode,
    quality = quality,
    favorite = favorite
)

fun CompressionHistory.toEntity() = CompressionHistoryEntity(
    id = id,
    originalUri = originalUri,
    compressedUri = compressedUri,
    originalSize = originalSize,
    compressedSize = compressedSize,
    savedBytes = savedBytes,
    savedPercent = savedPercent,
    format = format,
    resolution = resolution,
    createdAt = createdAt,
    metadataMode = metadataMode,
    quality = quality,
    favorite = favorite
)
