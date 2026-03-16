package org.dhis2.mobile.login.pin

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.FragmentActivity
import org.dhis2.mobile.login.pin.domain.model.PinMode
import org.dhis2.mobile.login.pin.ui.components.PinDialog
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

/**
 * Adds a [PinDialog] composable as an overlay on the Activity's decor view.
 *
 * Returns the [ComposeView] that was added, so the caller can remove it if needed
 * (e.g., on Activity stop). The [PinDialog] is displayed via [FullScreenDialog] internally,
 * which creates its own dialog window on top of the Activity.
 *
 * @param mode PIN mode: SET (create PIN) or ASK (verify PIN).
 * @param onSuccess Invoked when the PIN operation completes successfully.
 * @param onDismiss Invoked when the dialog is dismissed (back press, forgot PIN, too many attempts).
 * @return The [ComposeView] added to the window, or null if the decor view is unavailable.
 */
fun FragmentActivity.addPinBottomSheet(
    mode: PinMode,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
): ComposeView? {
    val decorView = window?.decorView as? ViewGroup ?: return null
    return ComposeView(this).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
        )
        setContent {
            DHIS2Theme {
                PinDialog(
                    mode = mode,
                    onSuccess = onSuccess,
                    onDismiss = onDismiss,
                )
            }
        }
        decorView.addView(this, MATCH_PARENT, MATCH_PARENT)
    }
}
