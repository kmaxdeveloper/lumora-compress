package uz.kmax.compress.core.compressor.writer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class OutputWriterTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var writer: OutputWriterImpl

    @Before
    fun setup() {
        writer = OutputWriterImpl(context, testDispatcher)
    }

    @Test
    fun `write to Cache creates file and returns valid Uri`() = runTest(testDispatcher) {
        // Arrange
        val request = OutputRequest(
            data = "test data".toByteArray(),
            mimeType = "image/jpeg",
            fileName = "test.jpg",
            destination = OutputDestination.Cache
        )

        // Act
        val result = writer.write(request)

        // Assert
        assertThat(result.uri.scheme).isEqualTo("file")
        assertThat(result.fileSize).isEqualTo(request.data.size.toLong())
        assertThat(File(result.uri.path!!).exists()).isTrue()
    }

    @Test
    fun `write to CustomFile respects overwrite flag`() = runTest(testDispatcher) {
        // Arrange
        val tempFile = File(context.cacheDir, "custom.jpg").apply { writeText("original") }
        val request = OutputRequest(
            data = "new data".toByteArray(),
            mimeType = "image/jpeg",
            fileName = "custom.jpg",
            destination = OutputDestination.CustomFile(tempFile),
            overwrite = false
        )

        // Act & Assert
        try {
            writer.write(request)
            assertThat(false).isTrue() // Should not reach here
        } catch (e: OutputException.FileAlreadyExists) {
            assertThat(tempFile.readText()).isEqualTo("original")
        }
    }
}
