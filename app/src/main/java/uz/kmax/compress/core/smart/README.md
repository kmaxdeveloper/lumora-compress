# Smart Compression Engine

Intelligent offline engine that automatically optimizes images based on their content, complexity, and resolution.

## Components

- **SmartCompressionEngine**: Main orchestrator with caching support.
- **SmartCompressionAnalyzer**: Performs deep image analysis (Entropy, Complexity, Transparency).
- **ImageClassifier**: Classifies images into categories (Portrait, Landscape, Screenshot, etc.).
- **SmartCompressionRules**: Rule-based decision engine for optimal format, quality, and resize.
- **SmartCompressionPrediction**: Estimates output size and quality score before processing.

## Classification Heuristics

1. **PHOTO**: High resolution, high color count, medium texture complexity.
2. **PORTRAIT**: High resolution, portrait aspect ratio, high sharpness in center areas.
3. **SCREENSHOT**: High sharpness, specific mobile/desktop aspect ratios, often PNG source.
4. **DOCUMENT**: High entropy, high sharpness, limited color palette.
5. **ANIME/ILLUSTRATION**: High texture complexity, low noise level.
6. **LOGO/ICON**: Small resolution, low color count.
7. **TRANSPARENT_IMAGE**: Images with alpha channel detected.

## Optimized Decisions

- **JPEG**: Used for Photos and Landscapes with specific quality based on entropy.
- **PNG**: Preserved for Screenshots, Documents, and Transparent images to ensure no artifacts in text/lines.
- **WEBP**: Used for Anime and as a fallback for high-efficiency compression.
- **AVIF**: Automatically selected on Android 12+ for high-quality photos to maximize storage savings.
- **Resize**: Intelligent downscaling only for very high megapixel images (>12MP).
- **Quality**: Dynamic selection from 70 to 100 based on image content type.
