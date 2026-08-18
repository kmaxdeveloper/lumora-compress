package uz.kmax.compress.core.storage

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface StorageManager {
    fun createTempFile(extension: String = "jpg", baseName: String? = null): File
    fun createAppFile(fileName: String): File
    fun saveToPublicStorage(file: File, mimeType: String, displayName: String): Uri?
    fun clearCache(): Boolean
    fun uriToFile(uri: Uri): File?
    fun deleteFile(uri: Uri): Boolean
    suspend fun getRecentImages(limit: Int = 20): List<MediaStoreImage>
    suspend fun getImageMetadata(uri: Uri): MediaStoreImage?
}

data class MediaStoreImage(
    val id: Long,
    val uri: Uri,
    val name: String,
    val size: Long,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val dateAdded: Long
)

class StorageManagerImpl(private val context: Context) : StorageManager {

    override fun createTempFile(extension: String, baseName: String?): File {
        val tempDir = File(context.cacheDir, "lumora_temp").apply { if (!exists()) mkdirs() }
        val name = if (baseName != null) {
            "${baseName.substringBeforeLast(".")}_${System.currentTimeMillis()}"
        } else {
            "TEMP_${UUID.randomUUID()}"
        }
        return File(tempDir, "$name.$extension")
    }

    override fun createAppFile(fileName: String): File {
        return File(context.filesDir, fileName)
    }

    override fun saveToPublicStorage(file: File, mimeType: String, displayName: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LumoraCompress")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        }
        return uri
    }

    override fun clearCache(): Boolean {
        return context.cacheDir.deleteRecursively()
    }

    override fun uriToFile(uri: Uri): File? {
        if (uri.scheme == "file") {
            return uri.path?.let { File(it) }
        }
        
        return try {
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri)) ?: "tmp"
            
            var originalName: String? = null
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    originalName = cursor.getString(nameIndex)
                }
            }

            val tempFile = createTempFile(extension, originalName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    override fun deleteFile(uri: Uri): Boolean {
        return try {
            if (uri.scheme == "file") {
                uri.path?.let { File(it) }?.delete() ?: false
            } else {
                context.contentResolver.delete(uri, null, null) > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getRecentImages(limit: Int): List<MediaStoreImage> = withContext(Dispatchers.IO) {
        val images = mutableListOf<MediaStoreImage>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED
        )
        
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val queryResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val queryArgs = android.os.Bundle().apply {
                putInt(android.content.ContentResolver.QUERY_ARG_LIMIT, limit)
                putStringArray(android.content.ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Images.Media.DATE_ADDED))
                putInt(android.content.ContentResolver.QUERY_ARG_SORT_DIRECTION, android.content.ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
            }
            context.contentResolver.query(queryUri, projection, queryArgs, null)
        } else {
            context.contentResolver.query(queryUri, projection, null, null, "$sortOrder LIMIT $limit")
        }

        queryResult?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                images.add(MediaStoreImage(
                    id = id,
                    uri = uri,
                    name = cursor.getString(nameColumn),
                    size = cursor.getLong(sizeColumn),
                    width = cursor.getInt(widthColumn),
                    height = cursor.getInt(heightColumn),
                    mimeType = cursor.getString(mimeColumn),
                    dateAdded = cursor.getLong(dateColumn)
                ))
            }
        }
        images
    }

    override suspend fun getImageMetadata(uri: Uri): MediaStoreImage? = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED
        )

        var result: MediaStoreImage? = null

        // Try MediaStore first
        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val nameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val sizeColumn = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                    val widthColumn = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
                    val heightColumn = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                    val mimeColumn = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                    val dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)

                    result = MediaStoreImage(
                        id = if (idColumn != -1) cursor.getLong(idColumn) else 0L,
                        uri = uri,
                        name = if (nameColumn != -1) cursor.getString(nameColumn) ?: "unknown" else "unknown",
                        size = if (sizeColumn != -1) cursor.getLong(sizeColumn) else 0L,
                        width = if (widthColumn != -1) cursor.getInt(widthColumn) else 0,
                        height = if (heightColumn != -1) cursor.getInt(heightColumn) else 0,
                        mimeType = if (mimeColumn != -1) cursor.getString(mimeColumn) ?: "image/*" else "image/*",
                        dateAdded = if (dateColumn != -1) cursor.getLong(dateColumn) else System.currentTimeMillis() / 1000
                    )
                }
            }
        } catch (e: Exception) {
            // MediaStore query failed
        }

        // Fallback for picker URIs or partial metadata
        if (result == null || result?.size == 0L || result?.width == 0) {
            var name = "image"
            var size = 0L
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        name = if (nameIndex != -1) cursor.getString(nameIndex) ?: "image" else "image"
                        size = if (sizeIndex != -1) cursor.getLong(sizeIndex) else 0L
                    }
                }
            } catch (e: Exception) {
                // OpenableColumns query failed
            }

            val mimeType = context.contentResolver.getType(uri) ?: "image/*"
            
            // Get dimensions using BitmapFactory
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            try {
                context.contentResolver.openInputStream(uri)?.use { 
                    BitmapFactory.decodeStream(it, null, options)
                }
            } catch (e: Exception) {
                // Decode failed
            }

            result = MediaStoreImage(
                id = result?.id ?: 0L,
                uri = uri,
                name = if (result?.name == null || result?.name == "unknown" || result?.name == "image") name else result?.name!!,
                size = if (result?.size == 0L || result?.size == null) size else result?.size!!,
                width = if (result?.width == 0 || result?.width == null) options.outWidth else result?.width!!,
                height = if (result?.height == 0 || result?.height == null) options.outHeight else result?.height!!,
                mimeType = if (result?.mimeType == null || result?.mimeType == "image/*") mimeType else result?.mimeType!!,
                dateAdded = result?.dateAdded ?: (System.currentTimeMillis() / 1000)
            )
        }

        result
    }
}
