package uz.kmax.compress.core.compressor.writer

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import uz.kmax.compress.core.di.qualifier.IoDispatcher
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutputWriterImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : OutputWriter {

    override suspend fun write(request: OutputRequest): OutputResult = withContext(ioDispatcher) {
        try {
            when (val dest = request.destination) {
                is OutputDestination.Cache -> writeToCache(request)
                is OutputDestination.PrivateStorage -> writeToPrivate(request)
                is OutputDestination.MediaStore -> writeToMediaStore(request)
                is OutputDestination.SAF -> writeToSAF(request, dest.treeUri)
                is OutputDestination.CustomFile -> writeToCustomFile(request, dest.file)
            }
        } catch (e: IOException) {
            throw OutputException.WriteFailed("Failed to write to disk: ${e.message}", e)
        }
    }

    private fun writeToCache(request: OutputRequest): OutputResult {
        val file = File(context.cacheDir, generateFileName(request))
        return writeToFile(file, request.data, request.mimeType)
    }

    private fun writeToPrivate(request: OutputRequest): OutputResult {
        val dir = if (request.relativePath != null) {
            File(context.filesDir, request.relativePath).apply { mkdirs() }
        } else {
            context.filesDir
        }
        val file = File(dir, generateFileName(request))
        return writeToFile(file, request.data, request.mimeType)
    }

    private fun writeToMediaStore(request: OutputRequest): OutputResult {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, request.fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, request.mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val path = request.relativePath ?: Environment.DIRECTORY_PICTURES
                put(MediaStore.MediaColumns.RELATIVE_PATH, path)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, contentValues) ?: throw OutputException.WriteFailed("MediaStore insert failed")
        
        resolver.openOutputStream(uri)?.use { it.write(request.data) } ?: throw OutputException.WriteFailed("Failed to open MediaStore stream")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return OutputResult(
            uri = uri,
            fileSize = request.data.size.toLong(),
            mimeType = request.mimeType
        )
    }

    private fun writeToSAF(request: OutputRequest, treeUri: Uri): OutputResult {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: throw OutputException.InvalidDestination()
        val file = root.createFile(request.mimeType, request.fileName) ?: throw OutputException.WriteFailed("SAF file creation failed")
        
        context.contentResolver.openOutputStream(file.uri)?.use { it.write(request.data) }
            ?: throw OutputException.WriteFailed("Failed to open SAF stream")

        return OutputResult(
            uri = file.uri,
            fileSize = request.data.size.toLong(),
            mimeType = request.mimeType
        )
    }

    private fun writeToCustomFile(request: OutputRequest, file: File): OutputResult {
        if (file.exists() && !request.overwrite) {
            throw OutputException.FileAlreadyExists()
        }
        file.parentFile?.mkdirs()
        return writeToFile(file, request.data, request.mimeType)
    }

    private fun writeToFile(file: File, data: ByteArray, mimeType: String): OutputResult {
        try {
            FileOutputStream(file).use { it.write(data) }
        } catch (e: Exception) {
            if (e.message?.contains("ENOSPC") == true) throw OutputException.DiskFull()
            throw e
        }
        return OutputResult(
            uri = Uri.fromFile(file),
            absolutePath = file.absolutePath,
            fileSize = data.size.toLong(),
            mimeType = mimeType
        )
    }

    private fun generateFileName(request: OutputRequest): String {
        return if (request.overwrite) {
            request.fileName
        } else {
            val extension = request.fileName.substringAfterLast(".", "")
            val name = request.fileName.substringBeforeLast(".")
            "${name}_${UUID.randomUUID()}.$extension"
        }
    }
}
