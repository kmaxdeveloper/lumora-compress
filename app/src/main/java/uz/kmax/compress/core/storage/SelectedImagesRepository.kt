package uz.kmax.compress.core.storage

import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository to temporarily hold selected images for Batch processing.
 * This prevents TransactionTooLargeException by avoiding passing large lists through Navigation/Bundles.
 */
@Singleton
class SelectedImagesRepository @Inject constructor() {

    private val storage = ConcurrentHashMap<String, List<GalleryImageUiModel>>()
    private val MAX_BATCHES = 10

    /**
     * Stores the given list of images and returns a unique batch ID.
     */
    fun store(images: List<GalleryImageUiModel>): String {
        // Simple eviction: if we exceed limit, clear all (rare case of abandoned batches)
        if (storage.size >= MAX_BATCHES) {
            storage.clear()
        }
        val id = UUID.randomUUID().toString()
        storage[id] = images
        return id
    }

    /**
     * Returns a batch for presentation only. Ownership stays with this repository until the
     * foreground service atomically claims it, so opening/recreating BatchFragment cannot lose
     * the work before the user presses Start.
     */
    fun peek(id: String): List<GalleryImageUiModel>? = storage[id]

    /**
     * Transfers ownership to exactly one consumer. A successful claim removes the entry, which
     * prevents duplicate service starts from processing the same selection twice.
     */
    fun claim(id: String): List<GalleryImageUiModel>? = storage.remove(id)
    
    /**
     * Clears all stored batches.
     */
    fun clearAll() {
        storage.clear()
    }
}
