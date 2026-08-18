package uz.kmax.compress.core.compressor.impl

import android.content.Context
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uz.kmax.compress.core.compressor.CompressionEngineStage
import uz.kmax.compress.core.compressor.CompressionRequest
import uz.kmax.compress.core.compressor.algorithm.CompressedImageData
import uz.kmax.compress.core.compressor.metadata.MetadataProcessor
import uz.kmax.compress.core.compressor.pipeline.CompressionPipeline
import uz.kmax.compress.core.compressor.pipeline.CompressionProgress
import uz.kmax.compress.core.compressor.pipeline.CompressionStage
import uz.kmax.compress.core.compressor.writer.OutputWriter
import uz.kmax.compress.core.compressor.writer.OutputResult
import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.analytics.CrashlyticsManager

@OptIn(ExperimentalCoroutinesApi::class)
class CompressionEngineTest {

    private val context = mockk<Context>(relaxed = true)
    private val pipeline = mockk<CompressionPipeline>()
    private val writer = mockk<OutputWriter>()
    private val metadataProcessor = mockk<MetadataProcessor>()
    private val crashlyticsManager = mockk<CrashlyticsManager>(relaxed = true)
    private val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()
    private lateinit var engine: CompressionEngineImpl

    @Before
    fun setup() {
        engine = CompressionEngineImpl(context, pipeline, writer, metadataProcessor, crashlyticsManager, testDispatcher)
    }

    @Test
    fun `compress orchestrates complete flow`() = runTest(testDispatcher) {
        // Arrange
        val request = mockk<CompressionRequest>(relaxed = true)
        val imageData = CompressedImageData(byteArrayOf(1), CompressionFormat.JPEG)
        val outputResult = OutputResult(mockk(), "path", 100, "image/jpeg")

        every { pipeline.execute(request) } returns flowOf(
            CompressionProgress(CompressionStage.INITIALIZING, 0.1f),
            CompressionProgress(CompressionStage.FINISHED, 1.0f, imageData = imageData)
        )
        coEvery { writer.write(any()) } returns outputResult
        coEvery { metadataProcessor.process(any(), any(), any()) } returns mockk()

        // Act & Assert
        engine.compress(request).test {
            assertThat(awaitItem().stage).isEqualTo(CompressionEngineStage.PIPELINE_PROCESSING)
            assertThat(awaitItem().stage).isEqualTo(CompressionEngineStage.WRITING_FILE)
            assertThat(awaitItem().stage).isEqualTo(CompressionEngineStage.APPLYING_METADATA)
            assertThat(awaitItem().stage).isEqualTo(CompressionEngineStage.COMPLETED)
            awaitComplete()
        }
    }
}
