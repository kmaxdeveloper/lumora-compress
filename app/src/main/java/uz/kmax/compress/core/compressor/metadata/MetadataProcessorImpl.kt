package uz.kmax.compress.core.compressor.metadata

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import uz.kmax.compress.core.di.qualifier.IoDispatcher
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataProcessorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val exifReader: ExifReader,
    private val exifWriter: ExifWriter
) : MetadataProcessor {

    override suspend fun process(
        inputUri: Uri,
        outputFile: File,
        options: MetadataOptions
    ): MetadataResult = withContext(ioDispatcher) {
        if (options.strategy == MetadataOptions.Strategy.REMOVE_ALL) {
            return@withContext MetadataResult(0, 0) // No copying needed, outputFile is clean by default from Bitmap.compress
        }

        try {
            val allTags = context.contentResolver.openInputStream(inputUri)?.use { input ->
                exifReader.readTags(input)
            } ?: emptyMap()

            if (allTags.isEmpty()) {
                return@withContext MetadataResult(0, 0)
            }

            val filteredTags = filterTags(allTags, options)
            exifWriter.writeTags(outputFile, filteredTags)

            MetadataResult(
                tagsCopied = filteredTags.size,
                tagsRemoved = allTags.size - filteredTags.size
            )
        } catch (e: Exception) {
            MetadataResult(0, 0, listOf("Metadata processing failed: ${e.message}"))
        }
    }

    private fun filterTags(tags: Map<String, String>, options: MetadataOptions): Map<String, String> {
        val filtered = when (options.strategy) {
            MetadataOptions.Strategy.KEEP_ALL -> tags.toMutableMap()
            MetadataOptions.Strategy.REMOVE_ALL -> mutableMapOf()
            MetadataOptions.Strategy.KEEP_ONLY_ORIENTATION -> {
                tags.filterKeys { it == ExifInterface.TAG_ORIENTATION }.toMutableMap()
            }
            MetadataOptions.Strategy.KEEP_DATE_AND_ORIENTATION -> {
                tags.filterKeys { it == ExifInterface.TAG_ORIENTATION || it == ExifInterface.TAG_DATETIME }.toMutableMap()
            }
            MetadataOptions.Strategy.REMOVE_GPS -> {
                tags.filterKeys { !it.startsWith("GPS") }.toMutableMap()
            }
            MetadataOptions.Strategy.CUSTOM -> {
                tags.filterKeys { options.customRules.contains(it) }.toMutableMap()
            }
        }

        // IMPORTANT: Since the pipeline physically rotates the bitmap to its correct orientation,
        // we must reset the Orientation tag in the metadata to 'NORMAL' (1). 
        // If we keep the original orientation tag, viewers will rotate the already-rotated image again.
        if (filtered.containsKey(ExifInterface.TAG_ORIENTATION) || options.strategy == MetadataOptions.Strategy.KEEP_ALL) {
            filtered[ExifInterface.TAG_ORIENTATION] = ExifInterface.ORIENTATION_NORMAL.toString()
        }

        return filtered
    }
}
