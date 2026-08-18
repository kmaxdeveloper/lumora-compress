package uz.kmax.compress.core.compressor.writer

import android.net.Uri

data class OutputResult(
    val uri: Uri,
    val absolutePath: String? = null,
    val fileSize: Long,
    val mimeType: String,
    val savedTimestamp: Long = System.currentTimeMillis()
)
