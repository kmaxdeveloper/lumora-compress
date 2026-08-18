package uz.kmax.compress.core.compressor.writer

import android.net.Uri
import java.io.File

sealed interface OutputDestination {
    data object Cache : OutputDestination
    data object PrivateStorage : OutputDestination
    data object MediaStore : OutputDestination
    data class SAF(val treeUri: Uri) : OutputDestination
    data class CustomFile(val file: File) : OutputDestination
}
