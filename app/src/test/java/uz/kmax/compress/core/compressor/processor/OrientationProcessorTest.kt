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
import uz.kmax.compress.core.compressor.decoder.DecodedBitmap

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OrientationProcessorTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var processor: OrientationProcessorImpl

    @Before
    fun setup() {
        processor = OrientationProcessorImpl(testDispatcher)
    }

    @Test
    fun `process returns original bitmap when rotation is 0`() = runTest(testDispatcher) {
        // Arrange
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val decoded = DecodedBitmap(bitmap, 10, 10, "image/jpeg", 0)

        // Act
        val result = processor.process(decoded)

        // Assert
        assertThat(result).isSameInstanceAs(bitmap)
    }
}
