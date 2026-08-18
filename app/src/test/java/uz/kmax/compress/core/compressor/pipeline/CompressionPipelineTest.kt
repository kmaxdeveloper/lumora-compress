package uz.kmax.compress.core.compressor.pipeline

import android.content.Context
import android.graphics.Bitmap
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.CompressionRequest
import uz.kmax.compress.core.compressor.algorithm.CompressedImageData
import uz.kmax.compress.core.compressor.algorithm.CompressionAlgorithm
import uz.kmax.compress.core.compressor.algorithm.CompressionAlgorithmFactory
import uz.kmax.compress.core.compressor.decoder.BitmapDecoder
import uz.kmax.compress.core.compressor.decoder.DecodedBitmap
import uz.kmax.compress.core.compressor.processor.OrientationProcessor
import uz.kmax.compress.core.compressor.processor.ResizeProcessor
import uz.kmax.compress.core.compressor.processor.ResizeResult
import uz.kmax.compress.core.analytics.CrashlyticsManager

class CompressionPipelineTest {

    private val context = mockk<Context>()
    private val decoder = mockk<BitmapDecoder>()
    private val orientationProcessor = mockk<OrientationProcessor>()
    private val resizeProcessor = mockk<ResizeProcessor>()
    private val algorithmFactory = mockk<CompressionAlgorithmFactory>()
    private val algorithm = mockk<CompressionAlgorithm>()
    private val crashlyticsManager = mockk<CrashlyticsManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var pipeline: CompressionPipelineImpl

    @Before
    fun setup() {
        pipeline = CompressionPipelineImpl(
            context,
            decoder, 
            orientationProcessor, 
            resizeProcessor, 
            algorithmFactory, 
            crashlyticsManager,
            testDispatcher
        )
    }

    @Test
    fun `execute emits correct sequence of stages`() = runTest(testDispatcher) {
        // Arrange
        val request = CompressionRequest(mockk(), mockk(), CompressionFormat.JPEG)
        val bitmap = mockk<Bitmap>(relaxed = true)
        val decoded = DecodedBitmap(bitmap, 100, 100, "image/jpeg")
        val resized = ResizeResult(bitmap, 100 to 100, 50 to 50, 0.5f)
        val compressed = CompressedImageData(byteArrayOf(1, 2, 3), CompressionFormat.JPEG)

        coEvery { decoder.decode(any(), any()) } returns decoded
        coEvery { orientationProcessor.process(any()) } returns bitmap
        coEvery { resizeProcessor.process(any(), any()) } returns resized
        every { algorithmFactory.getAlgorithm(any(), any()) } returns algorithm
        coEvery { algorithm.compress(any(), any()) } returns compressed

        // Act & Assert
        pipeline.execute(request).test {
            assertThat(awaitItem().stage).isEqualTo(CompressionStage.INITIALIZING)
            assertThat(awaitItem().stage).isEqualTo(CompressionStage.DECODING)
            assertThat(awaitItem().stage).isEqualTo(CompressionStage.ROTATING)
            assertThat(awaitItem().stage).isEqualTo(CompressionStage.RESIZING)
            assertThat(awaitItem().stage).isEqualTo(CompressionStage.ENCODING)
            assertThat(awaitItem().stage).isEqualTo(CompressionStage.FINISHED)
            awaitComplete()
        }
    }
}
