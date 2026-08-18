package uz.kmax.compress.core.compressor.metadata

import androidx.exifinterface.media.ExifInterface
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExifWriter @Inject constructor() {

    fun writeTags(file: File, tags: Map<String, String>) {
        if (!file.exists()) return
        
        val exifInterface = ExifInterface(file.absolutePath)
        tags.forEach { (tag, value) ->
            exifInterface.setAttribute(tag, value)
        }
        exifInterface.saveAttributes()
    }

    fun removeTags(file: File, tagNames: List<String>) {
        if (!file.exists()) return
        
        val exifInterface = ExifInterface(file.absolutePath)
        tagNames.forEach { tag ->
            exifInterface.setAttribute(tag, null)
        }
        exifInterface.saveAttributes()
    }
}
