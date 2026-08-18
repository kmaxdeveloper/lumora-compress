package uz.kmax.compress.domain.model

data class StorageGuardianResult(
    val totalImageCount: Int,
    val totalImageSize: Long,
    val averageImageSize: Long,
    val categories: List<StorageCategory>,
    val recentlyAddedCount: Int,
    val potentialSavingBytes: Long,
    val healthScore: Int, // 0-100
    val healthLabel: HealthLabel,
    val recommendations: List<String>,
    val isCapped: Boolean = false
)

data class StorageCategory(
    val type: CategoryType,
    val count: Int,
    val sizeBytes: Long,
    val potentialSavingBytes: Long,
    val images: List<StorageImage> = emptyList()
)

enum class CategoryType {
    CAMERA,
    SCREENSHOTS,
    WHATSAPP,
    TELEGRAM,
    DOWNLOADS,
    OTHER
}

enum class HealthLabel {
    EXCELLENT,
    GOOD,
    AVERAGE,
    NEEDS_OPTIMIZATION
}
