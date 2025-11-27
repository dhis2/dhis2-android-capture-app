package org.dhis2.mobile.login.authentication.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.WifiFind
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.dhis2.mobile.login.authentication.ui.viewmodel.TwoFASettingsViewModel
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.back_to_settings
import org.dhis2.mobile.login.resources.two_fa_checking_status
import org.dhis2.mobile.login.resources.two_fa_screen_title
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarActionIcon
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFASettingsScreen(
    onBackClick: () -> Unit = {},
    onOpenStore: () -> Unit = {},
    onCopyCode: (String) -> Unit = {},
) {
    val viewModel: TwoFASettingsViewModel = koinViewModel<TwoFASettingsViewModel>()

    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        val errorMessage =
            when (val state = uiState) {
                is TwoFAUiState.Enable -> state.errorMessage
                is TwoFAUiState.Disable -> state.errorMessage
                else -> null
            }

        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primary,
        topBar = {
            TopBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    TopBarActionIcon(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = stringResource(Res.string.back_to_settings),
                        onClick = onBackClick,
                    )
                },
                actions = { },
                title = {
                    Text(
                        text = stringResource(Res.string.two_fa_screen_title),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors().copy(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.dropShadow(shape = SnackbarDefaults.shape),
                    snackbarData = data,
                    containerColor = SurfaceColor.SurfaceBright,
                    contentColor = TextColor.OnSurface,
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { paddingValues ->
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
            color = SurfaceColor.SurfaceBright,
            shape = RoundedCornerShape(topStart = Radius.L, topEnd = Radius.L),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                            bottom = paddingValues.calculateBottomPadding(),
                        ).padding(horizontal = 16.dp)
                        .navigationBarsPadding(),
            ) {
                when (uiState) {
                    is TwoFAUiState.Checking -> {
                        item {
                            InfoBar(
                                text = stringResource(Res.string.two_fa_checking_status),
                                textColor = TextColor.OnSurfaceLight,
                                backgroundColor = SurfaceColor.Surface,
                                displayProgress = true,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.WifiFind,
                                        contentDescription = stringResource(Res.string.two_fa_checking_status),
                                    )
                                },
                            )
                        }
                    }

                    is TwoFAUiState.NoConnection -> {
                        item {
                            TwoFANoConnectionScreen(
                                onRetry = { viewModel.retry() },
                            )
                        }
                    }

                    is TwoFAUiState.Enable -> {
                        item {
                            TwoFAToEnableScreen(
                                uiState as TwoFAUiState.Enable,
                                {
                                    onOpenStore()
                                },
                                { code ->
                                    onCopyCode(code)
                                },
                                { code ->
                                    viewModel.enableTwoFA(code)
                                },
                            )
                        }
                    }

                    is TwoFAUiState.Disable -> {
                        item {
                            TwoFADisableScreen(
                                twoFADisableUiState = uiState as TwoFAUiState.Disable,
                                onAuthCodeUpdated = viewModel::updateAuthCode,
                                onDisable = { code ->
                                    viewModel.disableTwoFA(code)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
