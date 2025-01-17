package org.dhis2.ui.dialogs.bottomsheet

import android.os.Build
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetInsets(): @Composable () -> WindowInsets {
    val defaultInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets }
    val android35WindowInsets: @Composable () -> WindowInsets = { WindowInsets(0, 0, 0, 0) }
    return if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)) defaultInsets else android35WindowInsets
}

fun bottomSheetLowerPadding(): Dp {
    return if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)) Spacing.Spacing0 else Spacing.Spacing24
}
