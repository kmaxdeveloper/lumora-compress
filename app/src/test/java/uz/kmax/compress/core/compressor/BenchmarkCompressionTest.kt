package uz.kmax.compress.core.compressor

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uz.kmax.compress.core.compressor.algorithm.JpegCompressionAlgorithm
import uz.kmax.compress.core.compressor.processor.ResizeOptions
import uz.kmax.compress.core.compressor.processor.ResizeProcessorImpl
import uz.kmax.compress.core.compressor.processor.ResizeStrategy
import io.mockk.mockk
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
class BenchmarkCompressionTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var resizeProcessor: ResizeProcessorImpl
    private lateinit var jpegAlgorithm: JpegCompressionAlgorithm

    @Before
    fun setup() {
        resizeProcessor = ResizeProcessorImpl(testDispatcher)
        jpegAlgorithm = JpegCompressionAlgorithm(testDispatcher)
    }

    @Test
    fun `benchmark resize and compression performance`() = runTest(testDispatcher) {
        // Arrange
        val bitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        val resizeOptions = ResizeOptions(width = 1000, height = 1000, strategy = ResizeStrategy.KEEP_ASPECT_RATIO)
        val request = CompressionRequest(mockk(), mockk(), CompressionFormat.JPEG, CompressionQuality.Medium)

        // Act
        val resizeTime = measureTimeMillis {
            resizeProcessor.process(bitmap, resizeOptions)
        }
        
        val compressTime = measureTimeMillis {
            jpegAlgorithm.compress(bitmap, request)
        }

        // Assert
        println("Benchmark Results:")
        println("Resize Time: ${resizeTime}ms")
        println("Compress Time: ${compressTime}ms")
        
        assertThat(resizeTime).isLessThan(1000) // Sanity check
        assertThat(compressTime).isLessThan(1000)
    }
}
