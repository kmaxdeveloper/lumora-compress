package uz.kmax.compress.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

interface PermissionManager {
    fun hasPermission(permission: String): Boolean
    fun checkPermission(permission: String): PermissionState
    fun getRequiredPermissions(): List<String>
    fun hasStoragePermission(): Boolean
}

class PermissionManagerImpl(private val context: Context) : PermissionManager {

    override fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun checkPermission(permission: String): PermissionState {
        return when {
            hasPermission(permission) -> PermissionState.GRANTED
            // Check for partial access on Android 14+
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    permission == Manifest.permission.READ_MEDIA_IMAGES &&
                    hasPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> PermissionState.PARTIAL
            else -> PermissionState.DENIED
        }
    }

    override fun hasStoragePermission(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                hasPermission(Manifest.permission.READ_MEDIA_IMAGES) ||
                        hasPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                hasPermission(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else -> {
                hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    override fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions
    }
}
