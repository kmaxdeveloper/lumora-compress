package uz.kmax.compress.feature.home.model

data class RecentFileUiModel(
    val id: String,
    val name: String,
    val originalSize: String,
    val compressedSize: String,
    val reductionPercentage: String,
    val date: String,
    val uri: String
)
