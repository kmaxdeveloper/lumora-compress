package uz.kmax.compress.core.compressor.algorithm

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.CompressionQuality
import uz.kmax.compress.core.compressor.CompressionRequest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CompressionAlgorithmTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var jpegAlgorithm: JpegCompressionAlgorithm
    private lateinit var pngAlgorithm: PngCompressionAlgorithm

    @Before
    fun setup() {
        jpegAlgorithm = JpegCompressionAlgorithm(testDispatcher)
        pngAlgorithm = PngCompressionAlgorithm(testDispatcher)
    }

    @Test
    fun `JpegAlgorithm returns valid data and correct format`() = runTest(testDispatcher) {
        // Arrange
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val request = CompressionRequest(mockk(), mockk(), CompressionFormat.JPEG, CompressionQuality.Medium)

        // Act
        val result = jpegAlgorithm.compress(bitmap, request)

        // Assert
        assertThat(result.data).isNotEmpty()
        assertThat(result.format).isEqualTo(CompressionFormat.JPEG)
    }
}
