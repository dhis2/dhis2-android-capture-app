package org.dhis2.android.rtsm.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UIText {
    data class DynamicString(val value: String) : UIText()
    class StringRes(
        @androidx.annotation.StringRes val resId: Int,
        vararg val args: Any,
    ) : UIText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringRes -> stringResource(resId, *args)
        }
    }
}
