package org.dhis2.mobile.app.splash.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import org.dhis2.mobile.app.resources.Res
import org.dhis2.mobile.app.resources.ic_dhis_logo
import org.dhis2.mobile.app.splash.domain.model.StartDestination
import org.dhis2.mobile.app.splash.ui.state.SplashUiState
import org.dhis2.mobile.app.splash.ui.viewmodel.SplashViewModel
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SplashScreen(onNavigate: (StartDestination) -> Unit) {
    val context = LocalPlatformContext.current
    val viewModel = koinViewModel<SplashViewModel> { parametersOf(context) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        (uiState as? SplashUiState.Ready)?.let { onNavigate(it.destination) }
    }

    SplashContent()
}

@Composable
private fun SplashContent() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .safeDrawingPadding(),
    ) {
        Image(
            modifier =
                Modifier
                    .width(200.dp)
                    .align(Alignment.Center),
            imageVector = vectorResource(Res.drawable.ic_dhis_logo),
            contentDescription = "dhis2 logo",
        )
        // The design system indicator is drawn in the primary color, which is the background here.
        CircularProgressIndicator(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Preview
@Composable
private fun SplashContentPreview() {
    DHIS2Theme {
        SplashContent()
    }
}
