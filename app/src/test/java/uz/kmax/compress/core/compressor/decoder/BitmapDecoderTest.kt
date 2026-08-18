package uz.kmax.compress.core.compressor.decoder

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BitmapDecoderTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var decoder: BitmapDecoderImpl

    @Before
    fun setup() {
        decoder = BitmapDecoderImpl(context, testDispatcher)
    }

    @Test
    fun `getInfo returns correct image dimensions`() = runTest(testDispatcher) {
        val testFile = File(context.cacheDir, "test.jpg")
        createSampleImageFile(testFile)

        val info = decoder.getInfo(Uri.fromFile(testFile))

        assertThat(info.width).isEqualTo(2)
        assertThat(info.height).isEqualTo(2)
    }

    @Test
    fun `decode returns valid DecodedBitmap`() = runTest(testDispatcher) {
        val testFile = File(context.cacheDir, "test.jpg")
        createSampleImageFile(testFile)

        val result = decoder.decode(Uri.fromFile(testFile))

        assertThat(result.bitmap).isNotNull()
        assertThat(result.width).isEqualTo(2)
        assertThat(result.height).isEqualTo(2)
    }

    private fun createSampleImageFile(file: File) {
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        FileOutputStream(file).use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output))
        }
        bitmap.recycle()
    }
}
