package org.dhis2.mobile.aggregates.ui.snackbar

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow

@Composable
fun DataSetSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data ->
        Snackbar(
            modifier = Modifier.dropShadow(shape = SnackbarDefaults.shape),
            snackbarData = data,
            containerColor = SurfaceColor.SurfaceBright,
            contentColor = TextColor.OnSurface,
        )
    }
}
