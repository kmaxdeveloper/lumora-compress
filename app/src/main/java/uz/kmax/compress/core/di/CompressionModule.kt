package uz.kmax.compress.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.kmax.compress.core.compressor.decoder.BitmapDecoder
import uz.kmax.compress.core.compressor.decoder.BitmapDecoderImpl
import uz.kmax.compress.core.compressor.metadata.MetadataProcessor
import uz.kmax.compress.core.compressor.metadata.MetadataProcessorImpl
import uz.kmax.compress.core.compressor.CompressionEngine
import uz.kmax.compress.core.compressor.impl.CompressionEngineImpl
import uz.kmax.compress.core.compressor.pipeline.CompressionPipeline
import uz.kmax.compress.core.compressor.pipeline.CompressionPipelineImpl
import uz.kmax.compress.core.compressor.writer.OutputWriter
import uz.kmax.compress.core.compressor.writer.OutputWriterImpl
import uz.kmax.compress.core.compressor.processor.OrientationProcessor
import uz.kmax.compress.core.compressor.processor.OrientationProcessorImpl
import uz.kmax.compress.core.compressor.processor.ResizeProcessor
import uz.kmax.compress.core.compressor.processor.ResizeProcessorImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CompressionModule {

    @Binds
    @Singleton
    abstract fun bindBitmapDecoder(
        bitmapDecoderImpl: BitmapDecoderImpl
    ): BitmapDecoder

    @Binds
    @Singleton
    abstract fun bindOrientationProcessor(
        orientationProcessorImpl: OrientationProcessorImpl
    ): OrientationProcessor

    @Binds
    @Singleton
    abstract fun bindResizeProcessor(
        resizeProcessorImpl: ResizeProcessorImpl
    ): ResizeProcessor

    @Binds
    @Singleton
    abstract fun bindMetadataProcessor(
        metadataProcessorImpl: MetadataProcessorImpl
    ): MetadataProcessor

    @Binds
    @Singleton
    abstract fun bindCompressionPipeline(
        compressionPipelineImpl: CompressionPipelineImpl
    ): CompressionPipeline

    @Binds
    @Singleton
    abstract fun bindOutputWriter(
        outputWriterImpl: OutputWriterImpl
    ): OutputWriter

    @Binds
    @Singleton
    abstract fun bindCompressionEngine(
        compressionEngineImpl: CompressionEngineImpl
    ): CompressionEngine
}
