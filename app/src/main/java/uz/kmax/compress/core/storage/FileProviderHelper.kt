package uz.kmax.compress.core.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class FileProviderHelper(private val context: Context) {
    
    fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
