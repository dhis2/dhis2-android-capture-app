package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.dhis2.mobile.commons.extensions.ObserveAsEvents
import org.dhis2.mobile.login.accounts.ui.screen.AccountsScreen
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.ui.navigation.NavigationAction
import org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModel
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.ic_dhis_logo
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController = rememberNavController(),
    versionName: String,
    onImportDatabase: () -> Unit,
    legacyLoginContent: @Composable (server: String, username: String) -> Unit,
) {
    val viewModel = koinViewModel<LoginViewModel>()
    var displayMoreActions by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LoginTopBar(
                version = versionName,
                displayMoreActions = displayMoreActions,
                onImportDatabase = onImportDatabase,
            )
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->

        ObserveAsEvents(viewModel.navigator.navigationActions) { action ->
            when (action) {
                is NavigationAction.Navigate -> {
                    navController.navigate(action.destination) {
                        action.navOptions(this)
                        val currentRouteString = navController.currentBackStackEntry?.destination?.route
                        val startDestinationRouteString = LoginScreenState.Loading::class.qualifiedName
                        if (currentRouteString != null && currentRouteString == startDestinationRouteString) {
                            popUpTo(LoginScreenState.Loading::class) { inclusive = true }
                        }
                    }
                }
                NavigationAction.NavigateUp -> navController.navigateUp()
            }
        }

        val layoutDirection = LocalLayoutDirection.current

        NavHost(
            navController = navController,
            startDestination = LoginScreenState.Loading,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        start = padding.calculateStartPadding(layoutDirection),
                        end = padding.calculateEndPadding(layoutDirection),
                        bottom = 0.dp,
                    ).consumeWindowInsets(padding)
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    ).padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable<LoginScreenState.Loading> {
                displayMoreActions = false
                LoadingScreen()
            }
            composable<LoginScreenState.ServerValidation> {
                val args = it.toRoute<LoginScreenState.ServerValidation>()
                displayMoreActions = true

                val uiState by viewModel.serverValidationState.collectAsState()
                ServerValidationContent(
                    availableServers = args.availableServers,
                    state = uiState,
                    onValidate = viewModel::onValidateServer,
                    onCancel = viewModel::cancelServerValidation,
                )
            }
            composable<LoginScreenState.LegacyLogin> {
                val arg = it.toRoute<LoginScreenState.LegacyLogin>()
                displayMoreActions = arg.selectedServer.isEmpty()
                legacyLoginContent(arg.selectedServer, arg.selectedUsername)
            }
            composable<LoginScreenState.OauthLogin> {
                val args = it.toRoute<LoginScreenState.OauthLogin>()
                WebAuthenticator(url = args.selectedServer) {
                    viewModel.onOauthLoginCancelled()
                }
            }
            composable<LoginScreenState.Accounts> {
                displayMoreActions = true
                AccountsScreen()
            }
        }
    }
}

@Composable
fun LoginTopBar(
    version: String,
    displayMoreActions: Boolean = true,
    onImportDatabase: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        Box(
            modifier =
                Modifier
                    .height(80.dp)
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 8.dp),
        ) {
            Image(
                modifier =
                    Modifier
                        .height(48.dp)
                        .align(Alignment.Center),
                imageVector = vectorResource(resource = Res.drawable.ic_dhis_logo),
                contentDescription = "dhis2 logo",
            )

            if (displayMoreActions) {
                Box(
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    IconButton(
                        onClick = { expanded = true },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                    )

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                onImportDatabase()
                            },
                            text = {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = spacedBy(16.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Upload,
                                        contentDescription = "Import database",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )

                                    Text(
                                        text = "Import database",
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            },
                        )
                    }
                }
            }

            Text(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = -(8.dp)),
                text = version,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
