package uz.kmax.compress.core.permission

enum class PermissionState {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    PARTIAL // For Android 14+ photo/video access
}
