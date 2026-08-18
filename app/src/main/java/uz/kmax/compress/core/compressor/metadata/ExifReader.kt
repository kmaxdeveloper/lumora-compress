package uz.kmax.compress.core.compressor.metadata

import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExifReader @Inject constructor() {

    fun readTags(inputStream: InputStream): Map<String, String> {
        val exifInterface = ExifInterface(inputStream)
        val tags = mutableMapOf<String, String>()
        
        // List of common tags to read. In a real library, this would be more exhaustive.
        val commonTags = listOf(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_ARTIST,
            ExifInterface.TAG_COPYRIGHT,
            ExifInterface.TAG_SOFTWARE,
            ExifInterface.TAG_IMAGE_WIDTH,
            ExifInterface.TAG_IMAGE_LENGTH
        )

        commonTags.forEach { tag ->
            exifInterface.getAttribute(tag)?.let { value ->
                tags[tag] = value
            }
        }
        
        return tags
    }
}
