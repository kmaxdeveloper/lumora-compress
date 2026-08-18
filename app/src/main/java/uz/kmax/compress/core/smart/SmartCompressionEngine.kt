package uz.kmax.compress.core.smart

import android.net.Uri
import uz.kmax.compress.core.compressor.CompressionQuality
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The main orchestrator of the Smart Compression Engine.
 * It coordinates image analysis, classification, rule evaluation, and size prediction.
 * Uses a caching mechanism to prevent redundant analysis of the same image.
 */
@Singleton
class SmartCompressionEngine @Inject constructor(
    private val analyzer: SmartCompressionAnalyzer,
    private val classifier: ImageClassifier,
    private val rules: SmartCompressionRules,
    private val prediction: SmartCompressionPrediction
) {

    // Cache for analysis results to avoid redundant processing
    private val analysisCache = ConcurrentHashMap<Uri, SmartCompressionAnalyzer.ImageMetrics>()
    private val decisionCache = ConcurrentHashMap<Uri, SmartCompressionDecision>()

    /**
     * Analyzes the image and makes a smart compression decision based on its content.
     * 
     * @param uri The Uri of the image to analyze.
     * @return A [SmartCompressionDecision] containing optimal format, quality, and resize settings.
     */
    suspend fun makeDecision(uri: Uri): SmartCompressionDecision {
        // Check cache first
        decisionCache[uri]?.let { return it }

        val metrics = analysisCache.getOrPut(uri) {
            analyzer.analyze(uri)
        }

        val classification = classifier.classify(metrics)
        val ruleResult = rules.decide(metrics, classification)
        
        val pred = prediction.estimate(
            metrics = metrics,
            format = ruleResult.format,
            quality = ruleResult.quality,
            resizeFactor = ruleResult.resizeFactor
        )

        return SmartCompressionDecision(
            format = ruleResult.format,
            quality = CompressionQuality.Custom(ruleResult.quality),
            resizeFactor = ruleResult.resizeFactor,
            metadataStrategy = ruleResult.metadataStrategy,
            classification = classification,
            estimatedOutputSize = pred.estimatedSize,
            estimatedSavingPercent = pred.savingPercent,
            estimatedQualityScore = pred.qualityScore,
            reason = ruleResult.reason
        ).also {
            decisionCache[uri] = it
        }
    }

    /**
     * Clears the cache for a specific Uri or all.
     */
    fun clearCache(uri: Uri? = null) {
        if (uri != null) {
            analysisCache.remove(uri)
            decisionCache.remove(uri)
        } else {
            analysisCache.clear()
            decisionCache.clear()
        }
    }
}
