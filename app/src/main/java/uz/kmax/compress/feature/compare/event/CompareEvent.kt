package uz.kmax.compress.feature.compare.event

import android.net.Uri

sealed interface CompareEvent {
    data object Save : CompareEvent
    data class Share(val uri: Uri) : CompareEvent
    data object DeleteOriginal : CompareEvent
    data object CompressAgain : CompareEvent
    data object NavigateHome : CompareEvent
    data class ShowSnackbar(val message: String) : CompareEvent
    data class ShowDialog(val title: String, val message: String) : CompareEvent
}
