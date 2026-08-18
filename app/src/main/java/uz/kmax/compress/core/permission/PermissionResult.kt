package uz.kmax.compress.core.permission

sealed class PermissionResult {
    data object Granted : PermissionResult()
    data object Denied : PermissionResult()
    data object PermanentlyDenied : PermissionResult()
}
