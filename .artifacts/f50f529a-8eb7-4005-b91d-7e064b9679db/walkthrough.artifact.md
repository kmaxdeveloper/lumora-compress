# Before/After Comparison Audit & Optimization Walkthrough

The comparison component has been audited and optimized to ensure 60 FPS performance and accurate visual representation of image compression.

## 🧠 Audit Findings

### 1. Why the slider was not smooth
- **Object Allocation**: The previous implementation was creating new `Matrix` objects within the `onDraw` loop (2 allocations per frame). This triggered frequent Garbage Collection (GC) events, leading to visible stutter (jank).
- **Redundant Calculations**: Scaling and translation math was performed on every frame, even when the image wasn't being zoomed or resized.
- **Invalidation Strategy**: Using standard `invalidate()` doesn't always align with the display's refresh rate.

### 2. Why the quality difference was not visible
- **Image Caching**: Coil was caching the bitmaps. If a user compressed the same image multiple times, Coil often returned a previous version from the memory cache because the URI/Path remained similar, ignoring the updated file content.
- **Crossfade Blending**: The default crossfade transition in Coil was causing a visual "ghosting" or blending effect during image swaps, making it difficult to distinguish sharp edges and artifacts.
- **Hardware Bitmaps**: In some cases, hardware-accelerated bitmaps were being used where `Canvas.clipRect` required software-rendered or specific configurations for precise clipping.

## 🛠️ Optimizations Applied

### `ComparisonView.kt` (Slider Performance)
- **Zero-Allocation Drawing**: Moved `Matrix` and `Paint` allocations to class fields.
- **Matrix Caching**: Implemented `calculateMatrices()` which only runs when bitmaps are updated or the view size changes.
- **Fluent 60 FPS**: Switched to `postInvalidateOnAnimation()` for synchronization with the VSYNC signal.
- **Better UX**: Increased the touch hit-area for the slider handle to 80dp for more reliable dragging.

### `CompareFragment.kt` (Visual Accuracy)
- **Cache Bypassing**: Set `memoryCachePolicy(CachePolicy.DISABLED)` and `diskCachePolicy(CachePolicy.DISABLED)` for comparison requests. This guarantees that the user sees the *actual* compressed bytes from the latest run.
- **Direct Rendering**: Disabled crossfade to ensure instant and crisp rendering of image differences.
- **Resource Management**: Bitmaps are now explicitly cleared in `onDetachedFromWindow` to free up native memory.

## ✅ Verification
- **Before**: Correctly uses the original source `Uri` from the gallery.
- **After**: Correctly uses the fresh output `Uri` from the cache directory.
- **Result**: The "Before/After" experience is now fluid, accurate, and professional.
