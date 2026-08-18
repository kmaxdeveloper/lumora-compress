package uz.kmax.compress.core.storage

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

interface ImagePickerManager {
    fun pickImage(launcher: ActivityResultLauncher<PickVisualMediaRequest>)
    fun captureImage(launcher: ActivityResultLauncher<Uri>, uri: Uri)
}

class ImagePickerManagerImpl : ImagePickerManager {

    override fun pickImage(launcher: ActivityResultLauncher<PickVisualMediaRequest>) {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    override fun captureImage(launcher: ActivityResultLauncher<Uri>, uri: Uri) {
        launcher.launch(uri)
    }
}
