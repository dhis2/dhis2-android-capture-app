package org.dhis2.mobile.login.authentication

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.os.Build
import android.os.PersistableBundle
import androidx.annotation.ChecksSdkIntAtLeast
import org.dhis2.mobile.login.authentication.domain.repository.SecretClipboard

internal class SensitiveClipboard(
    private val clipboard: ClipboardManager,
    private val sdkInt: Int = Build.VERSION.SDK_INT,
) : SecretClipboard {
    override fun copy(text: String) {
        val clip = ClipData.newPlainText(CLIP_LABEL, text)
        if (isAtLeastN()) {
            clip.description.extras =
                PersistableBundle().apply {
                    putBoolean(sensitiveExtraKey(), true)
                }
        }
        clipboard.setPrimaryClip(clip)
    }

    override fun clearIfOwned() {
        if (clipboard.primaryClipDescription?.label != CLIP_LABEL) return
        if (isAtLeastP()) {
            clipboard.clearPrimaryClip()
        } else {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }

    private fun sensitiveExtraKey(): String =
        if (isAtLeastTiramisu()) {
            ClipDescription.EXTRA_IS_SENSITIVE
        } else {
            EXTRA_IS_SENSITIVE_LEGACY
        }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
    private fun isAtLeastN() = sdkInt >= Build.VERSION_CODES.N

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    private fun isAtLeastP() = sdkInt >= Build.VERSION_CODES.P

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    private fun isAtLeastTiramisu() = sdkInt >= Build.VERSION_CODES.TIRAMISU

    companion object {
        const val CLIP_LABEL = "dhis2_2fa_secret"
        const val EXTRA_IS_SENSITIVE_LEGACY = "android.content.extra.IS_SENSITIVE"
    }
}
