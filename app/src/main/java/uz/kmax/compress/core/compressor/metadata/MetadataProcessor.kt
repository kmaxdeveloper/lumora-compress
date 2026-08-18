package uz.kmax.compress.core.compressor.metadata

import android.net.Uri
import java.io.File

interface MetadataProcessor {
    /**
     * Processes metadata from the source [inputUri] and applies it to the [outputFile]
     * based on the provided [options].
     */
    suspend fun process(inputUri: Uri, outputFile: File, options: MetadataOptions): MetadataResult
}
