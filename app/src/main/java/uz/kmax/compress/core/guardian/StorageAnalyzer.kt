package uz.kmax.compress.core.guardian

import android.content.Context
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.kmax.compress.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object { const val MAX_CACHED_IMAGES_PER_CATEGORY = 20_000 }

    suspend fun analyze(): StorageGuardianResult = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.RELATIVE_PATH
            } else {
                MediaStore.Images.Media.DATA
            }
        )

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        var totalSize = 0L
        var count = 0
        val categoryData = mutableMapOf<CategoryType, CategoryStats>()
        CategoryType.entries.forEach { categoryData[it] = CategoryStats() }

        val now = System.currentTimeMillis() / 1000
        var recentlyAdded = 0

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val mimeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val bucketColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val pathColumn = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
            } else {
                it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            }

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn) ?: "unknown"
                val size = it.getLong(sizeColumn)
                val width = it.getInt(widthColumn)
                val height = it.getInt(heightColumn)
                val mimeType = it.getString(mimeColumn) ?: "image/jpeg"
                val date = it.getLong(dateColumn)
                val bucketName = it.getString(bucketColumn) ?: ""
                val relativePath = it.getString(pathColumn) ?: ""
                
                val uri = android.content.ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )

                totalSize += size
                count++

                if (now - date < 7 * 24 * 60 * 60) { // Last 7 days
                    recentlyAdded++
                }

                val type = detectCategory(bucketName, relativePath)
                categoryData[type]?.let { stats ->
                    stats.count++
                    stats.size += size
                    if (stats.images.size < MAX_CACHED_IMAGES_PER_CATEGORY) {
                        stats.images.add(StorageImage(uri, name, size, width, height, mimeType, date))
                    }
                }
            }
        }

        val categories = categoryData.map { (type, stats) ->
            StorageCategory(
                type = type,
                count = stats.count,
                sizeBytes = stats.size,
                potentialSavingBytes = (stats.size * 0.6f).toLong(), // Estimated 60% saving
                images = stats.images
            )
        }

        val potentialTotalSaving = categories.sumOf { it.potentialSavingBytes }
        val healthScore = calculateHealthScore(totalSize, potentialTotalSaving)
        val healthLabel = getHealthLabel(healthScore)
        val recommendations = generateRecommendations(categories, healthScore)

        StorageGuardianResult(
            totalImageCount = count,
            totalImageSize = totalSize,
            averageImageSize = if (count > 0) totalSize / count else 0,
            categories = categories,
            recentlyAddedCount = recentlyAdded,
            potentialSavingBytes = potentialTotalSaving,
            healthScore = healthScore,
            healthLabel = healthLabel,
            recommendations = recommendations,
            isCapped = categories.any { it.count > MAX_CACHED_IMAGES_PER_CATEGORY }
        )
    }

    private fun detectCategory(bucketName: String, relativePath: String): CategoryType {
        val bucket = bucketName.lowercase()
        val path = relativePath.lowercase()
        return when {
            bucket.contains("camera") || path.contains("dcim/camera") -> CategoryType.CAMERA
            bucket.contains("screenshot") || path.contains("screenshots") -> CategoryType.SCREENSHOTS
            bucket.contains("whatsapp") || path.contains("whatsapp") -> CategoryType.WHATSAPP
            bucket.contains("telegram") || path.contains("telegram") -> CategoryType.TELEGRAM
            bucket.contains("download") || path.contains("download") -> CategoryType.DOWNLOADS
            else -> CategoryType.OTHER
        }
    }

    private fun calculateHealthScore(totalSize: Long, saving: Long): Int {
        if (totalSize == 0L) return 100
        val ratio = saving.toFloat() / totalSize.toFloat()
        return (100 * (1.0f - ratio)).toInt().coerceIn(0, 100)
    }

    private fun getHealthLabel(score: Int): HealthLabel {
        return when {
            score >= 90 -> HealthLabel.EXCELLENT
            score >= 75 -> HealthLabel.GOOD
            score >= 50 -> HealthLabel.AVERAGE
            else -> HealthLabel.NEEDS_OPTIMIZATION
        }
    }

    private fun generateRecommendations(categories: List<StorageCategory>, score: Int): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (score < 50) {
            recommendations.add("Your storage needs optimization. Start compressing large folders.")
        }
        
        val maxSavingCategory = categories.maxByOrNull { it.potentialSavingBytes }
        if (maxSavingCategory != null && maxSavingCategory.potentialSavingBytes > 100 * 1024 * 1024) {
            recommendations.add("Compressing ${maxSavingCategory.type.name} could save significant space.")
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Storage usage is already optimized.")
        }

        return recommendations
    }

    private class CategoryStats {
        var count: Int = 0
        var size: Long = 0L
        val images: MutableList<StorageImage> = mutableListOf()
    }
}
