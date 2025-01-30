package org.dhis2.ui.dialogs.bottomsheet

import android.os.Build
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults

@Composable
fun BottomSheetInsets(): WindowInsets {
    return BottomSheetShellDefaults.windowInsets(Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
}

fun bottomSheetLowerPadding(): Dp {
    return BottomSheetShellDefaults.lowerPadding(Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
}
