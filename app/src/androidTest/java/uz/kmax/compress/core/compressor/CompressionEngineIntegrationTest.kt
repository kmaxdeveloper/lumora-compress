package uz.kmax.compress.core.compressor

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uz.kmax.compress.core.compressor.writer.OutputDestination
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltAndroidTest
class CompressionEngineIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var engine: CompressionEngine

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testCompressionSuccess() = runTest {
        // Arrange
        val testFile = File(context.cacheDir, "test_input.jpg")
        createSampleImageFile(testFile)
        val inputUri = Uri.fromFile(testFile)
        val outputUri = Uri.fromFile(File(context.cacheDir, "test_output.jpg"))

        val request = CompressionRequest(
            inputUri = inputUri,
            outputUri = outputUri,
            format = CompressionFormat.JPEG,
            quality = CompressionQuality.Medium
        )

        // Act & Assert
        engine.compress(request).test {
            val first = awaitItem()
            assertThat(first.stage).isEqualTo(CompressionEngineStage.PIPELINE_PROCESSING)
            
            var lastProgress: CompressionEngineProgress? = null
            while (true) {
                val item = awaitItem()
                lastProgress = item
                if (item.stage == CompressionEngineStage.COMPLETED || item.stage == CompressionEngineStage.FAILED) break
            }

            assertThat(lastProgress?.stage).isEqualTo(CompressionEngineStage.COMPLETED)
            assertThat(lastProgress?.result).isInstanceOf(CompressionResult.Success::class.java)
        }
    }

    private fun createSampleImageFile(file: File) {
        val bytes = byteArrayOf(
            -1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 1, 0, 72, 0, 72, 0, 0, -1, -37, 0, 67, 0, 
            8, 6, 6, 7, 6, 5, 8, 7, 7, 7, 9, 9, 8, 10, 12, 20, 13, 12, 11, 11, 12, 25, 18, 19, 15, 20, 
            29, 26, 31, 30, 29, 26, 28, 28, 32, 36, 46, 39, 32, 34, 44, 35, 28, 28, 40, 55, 41, 44, 48, 
            49, 52, 52, 52, 31, 39, 57, 61, 56, 50, 60, 46, 51, 52, 50, -1, -37, 0, 67, 1, 9, 9, 9, 12, 
            11, 12, 24, 13, 13, 24, 50, 33, 28, 33, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 
            50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 
            50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, -1, -64, 0, 17, 8, 0, 1, 0, 1, 3, 1, 
            34, 0, 2, 17, 1, 3, 17, 1, -1, -60, 0, 21, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            8, -1, -60, 0, 22, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -60, 0, 21, 1, 1, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -60, 0, 22, 17, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, -1, -38, 0, 12, 3, 1, 0, 2, 17, 3, 17, 0, 63, 0, -84, -1, -39
        )
        FileOutputStream(file).use { it.write(bytes) }
    }
}
