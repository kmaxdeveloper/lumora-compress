package uz.kmax.compress.core.compressor.processor

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ResizeProcessorTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var processor: ResizeProcessorImpl

    @Before
    fun setup() {
        processor = ResizeProcessorImpl(testDispatcher)
    }

    @Test
    fun `process with KEEP_ASPECT_RATIO resizes correctly`() = runTest(testDispatcher) {
        // Arrange
        val original = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888)
        val options = ResizeOptions(width = 50, height = 50, strategy = ResizeStrategy.KEEP_ASPECT_RATIO)

        // Act
        val result = processor.process(original, options)

        // Assert
        assertThat(result.newResolution.first).isEqualTo(50)
        assertThat(result.newResolution.second).isEqualTo(25)
    }
}
