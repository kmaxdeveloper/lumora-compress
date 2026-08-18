package uz.kmax.compress.core.compressor.decoder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import uz.kmax.compress.core.di.qualifier.IoDispatcher
import java.io.InputStream
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BitmapDecoderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BitmapDecoder {

    // 24 MP is within normal modern-camera use while leaving headroom for rotation/scaling copies.
    private companion object { const val MAX_DECODE_PIXELS = 24_000_000L }

    override suspend fun decode(uri: Uri, options: DecodeOptions): DecodedBitmap = withContext(ioDispatcher) {
        try {
            val info = readBounds(uri)
            if (info.first <= 0 || info.second <= 0) throw DecodeException.InvalidUri(uri.toString())
            if (info.first.toLong() * info.second.toLong() > MAX_DECODE_PIXELS) {
                throw DecodeException.ImageTooLarge(info.first, info.second)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && uri.scheme != "file") {
                decodeWithImageDecoder(uri, options)
            } else {
                decodeWithBitmapFactory(uri, options)
            }
        } catch (e: OutOfMemoryError) {
            throw DecodeException.OutOfMemory()
        } catch (e: Exception) {
            if (e is DecodeException) throw e
            throw DecodeException.DecodingFailed(e)
        }
    }

    override suspend fun getInfo(uri: Uri): BitmapDecoder.ImageInfo = withContext(ioDispatcher) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            } ?: throw DecodeException.InvalidUri(uri.toString())
            
            val rotation = getRotation(uri)
            
            BitmapDecoder.ImageInfo(
                width = options.outWidth,
                height = options.outHeight,
                mimeType = options.outMimeType,
                rotation = rotation
            )
        } catch (e: Exception) {
            if (e is DecodeException) throw e
            throw DecodeException.InvalidUri(uri.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeWithImageDecoder(uri: Uri, options: DecodeOptions): DecodedBitmap {
        val source = if (uri.scheme == "file") {
            ImageDecoder.createSource(fileFor(uri))
        } else {
            ImageDecoder.createSource(context.contentResolver, uri)
        }
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.setTargetColorSpace(android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB))
            if (!options.allowHardwareBitmap) {
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }

            options.requiredSize?.let { (reqWidth, reqHeight) ->
                decoder.setTargetSize(reqWidth, reqHeight)
            }
        }

        return DecodedBitmap(
            bitmap = bitmap,
            width = bitmap.width,
            height = bitmap.height,
            mimeType = context.contentResolver.getType(uri),
            rotation = getRotation(uri),
            hasAlpha = bitmap.hasAlpha()
        )
    }

    private fun decodeWithBitmapFactory(uri: Uri, options: DecodeOptions): DecodedBitmap {
        val factoryOptions = BitmapFactory.Options().apply {
            inPreferredConfig = options.preferredConfig
            
            options.requiredSize?.let { (reqWidth, reqHeight) ->
                val info = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input, null, info) }
                inSampleSize = calculateInSampleSize(info.outWidth, info.outHeight, reqWidth, reqHeight)
            }
        }

        val bitmap = openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, factoryOptions)
        } ?: throw DecodeException.DecodingFailed(Exception("BitmapFactory returned null"))

        return DecodedBitmap(
            bitmap = bitmap,
            width = bitmap.width,
            height = bitmap.height,
            mimeType = factoryOptions.outMimeType,
            rotation = getRotation(uri),
            hasAlpha = bitmap.hasAlpha()
        )
    }

    private fun getRotation(uri: Uri): Int {
        return try {
            openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun openInputStream(uri: Uri): InputStream? {
        return if (uri.scheme == "file") {
            FileInputStream(fileFor(uri))
        } else {
            context.contentResolver.openInputStream(uri)
        }
    }

    private fun readBounds(uri: Uri): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw DecodeException.InvalidUri(uri.toString())
        return options.outWidth to options.outHeight
    }

    private fun fileFor(uri: Uri): File {
        return try {
            File(java.net.URI(uri.toString()))
        } catch (e: Exception) {
            uri.path?.let(::File) ?: throw DecodeException.InvalidUri(uri.toString())
        }
    }
}
