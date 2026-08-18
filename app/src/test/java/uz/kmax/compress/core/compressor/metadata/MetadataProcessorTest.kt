package uz.kmax.compress.core.compressor.metadata

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

class MetadataProcessorTest {

    private val context = mockk<Context>()
    private val contentResolver = mockk<ContentResolver>()
    private val exifReader = mockk<ExifReader>()
    private val exifWriter = mockk<ExifWriter>()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var processor: MetadataProcessorImpl

    @Before
    fun setup() {
        every { context.contentResolver } returns contentResolver
        processor = MetadataProcessorImpl(context, testDispatcher, exifReader, exifWriter)
    }

    @Test
    fun `process with REMOVE_ALL strategy skips reading`() = runTest(testDispatcher) {
        // Arrange
        val options = MetadataOptions(strategy = MetadataOptions.Strategy.REMOVE_ALL)
        val inputUri = mockk<Uri>()
        val outputFile = mockk<File>()

        // Act
        val result = processor.process(inputUri, outputFile, options)

        // Assert
        assertThat(result.tagsCopied).isEqualTo(0)
        verify(exactly = 0) { exifReader.readTags(any()) }
    }

    @Test
    fun `process with KEEP_ALL strategy copies all tags`() = runTest(testDispatcher) {
        // Arrange
        val options = MetadataOptions(strategy = MetadataOptions.Strategy.KEEP_ALL)
        val inputUri = mockk<Uri>()
        val outputFile = mockk<File>()
        val tags = mapOf("Tag1" to "Value1", "Tag2" to "Value2")

        every { contentResolver.openInputStream(inputUri) } returns ByteArrayInputStream(byteArrayOf(0))
        every { exifReader.readTags(any()) } returns tags
        every { exifWriter.writeTags(outputFile, any()) } returns Unit

        // Act
        val result = processor.process(inputUri, outputFile, options)

        // Assert
        // Original 2 tags + 1 Orientation tag forced by processor
        assertThat(result.tagsCopied).isEqualTo(3)
        verify { exifWriter.writeTags(outputFile, any()) }
    }
}
