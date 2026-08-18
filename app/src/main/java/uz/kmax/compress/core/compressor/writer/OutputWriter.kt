package uz.kmax.compress.core.compressor.writer

interface OutputWriter {
    /**
     * Persists the compressed image bytes to the specified destination.
     *
     * @param request The write parameters.
     * @return [OutputResult] with final Uri and metadata.
     * @throws OutputException if writing fails.
     */
    suspend fun write(request: OutputRequest): OutputResult
}
