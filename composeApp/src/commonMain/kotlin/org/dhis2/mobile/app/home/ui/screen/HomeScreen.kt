package org.dhis2.mobile.app.home.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.dhis2.mobile.app.resources.Res
import org.dhis2.mobile.app.resources.home_not_available
import org.jetbrains.compose.resources.stringResource

// TODO: Replace with the real home once it is migrated
@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(Res.string.home_not_available))
    }
}
