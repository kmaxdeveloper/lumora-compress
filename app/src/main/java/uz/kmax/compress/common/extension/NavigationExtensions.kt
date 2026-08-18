package uz.kmax.compress.common.extension

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions

/**
 * Safely navigate to a destination.
 */
fun NavController.safeNavigate(directions: NavDirections, navOptions: NavOptions? = null) {
    currentDestination?.getAction(directions.actionId)?.let {
        if (navOptions != null) {
            navigate(directions, navOptions)
        } else {
            navigate(directions)
        }
    }
}

/**
 * Safely navigate to a destination by ID.
 */
fun NavController.safeNavigate(resId: Int, navOptions: NavOptions? = null) {
    currentDestination?.getAction(resId)?.let {
        if (navOptions != null) {
            navigate(resId, null, navOptions)
        } else {
            navigate(resId)
        }
    }
}
