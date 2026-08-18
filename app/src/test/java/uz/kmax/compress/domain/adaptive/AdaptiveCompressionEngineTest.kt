package uz.kmax.compress.domain.adaptive

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import uz.kmax.compress.core.compressor.*

class AdaptiveCompressionEngineTest {

    private val engine = mockk<CompressionEngine>()
    private val calculator = mockk<TargetSizeCalculator>()
    private val adaptiveEngine = AdaptiveCompressionEngine(engine, calculator)

    @Test
    fun `compress picks best result closest to target`() = runTest {
        val request = mockk<CompressionRequest>(relaxed = true)
        val target = 500_000L
        
        // Iteration 1: 600KB
        val res1 = CompressionResult.Success(SIZE_1MB, 100, 600_000L, SIZE_400KB, 40f, mockk(), mockk())
        // Iteration 2: 450KB (Closer)
        val res2 = CompressionResult.Success(SIZE_1MB, 200, 450_000L, SIZE_550KB, 55f, mockk(), mockk())
        // Iteration 3: 400KB (Further than Iteration 2 from 500KB target)
        val res3 = CompressionResult.Success(SIZE_1MB, 300, 400_000L, SIZE_600KB, 60f, mockk(), mockk())

        every { engine.compress(any()) } returnsMany listOf(
            flowOf(CompressionEngineProgress(CompressionEngineStage.COMPLETED, 1f, res1)),
            flowOf(CompressionEngineProgress(CompressionEngineStage.COMPLETED, 1f, res2)),
            flowOf(CompressionEngineProgress(CompressionEngineStage.COMPLETED, 1f, res3))
        )
        
        every { calculator.nextQuality(any(), any(), any()) } returns 50

        adaptiveEngine.compress(request, target, 80, maxIterations = 3).test {
            // Emits best so far at each step
            assertEquals(600_000L, awaitItem().result!!.compressedSize)
            assertEquals(450_000L, awaitItem().result!!.compressedSize)
            // Even if iteration 3 is further (100k diff vs 50k diff), it should still return the best (res2: 450KB)
            assertEquals(450_000L, awaitItem().result!!.compressedSize)
            awaitComplete()
        }
    }

    private companion object {
        const val SIZE_1MB = 1024 * 1024L
        const val SIZE_400KB = 400 * 1024L
        const val SIZE_550KB = 550 * 1024L
        const val SIZE_600KB = 600 * 1024L
    }
}
