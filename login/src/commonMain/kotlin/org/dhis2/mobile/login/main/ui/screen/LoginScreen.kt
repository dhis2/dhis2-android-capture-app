package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.dhis2.mobile.login.main.ui.contracts.filePicker
import org.dhis2.mobile.login.main.ui.navigation.NavigationAction
import org.dhis2.mobile.login.main.ui.state.DatabaseImportState
import org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModel
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.ic_dhis_logo
import org.dhis2.mobile.login.resources.import_database
import org.dhis2.mobile.login.resources.importing_successful
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.menu.DropDownMenu
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuLeadingElement
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController = rememberNavController(),
    versionName: String,
    onNavigateToSync: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToRecoverAccount: (serverUrl: String) -> Unit,
) {
    val viewModel = koinViewModel<LoginViewModel>()
    var displayMoreActions by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val databaseImportState by viewModel.importDatabaseState.collectAsState()

    val picker =
        filePicker { path ->
            path?.let { viewModel.importDb(it) }
        }

    LaunchedEffect(databaseImportState) {
        when (databaseImportState) {
            is DatabaseImportState.OnFailure -> {
                snackBarHostState.showSnackbar(
                    (databaseImportState as DatabaseImportState.OnFailure).message,
                )
            }
            is DatabaseImportState.OnSuccess -> {
                snackBarHostState.showSnackbar(
                    getString(Res.string.importing_successful),
                )
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LoginTopBar(
                version = versionName,
                displayMoreActions = displayMoreActions,
                onImportDatabase = { picker.launch() },
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                Snackbar(
                    modifier = Modifier.dropShadow(shape = SnackbarDefaults.shape),
                    snackbarData = data,
                    containerColor = SurfaceColor.SurfaceBright,
                    contentColor = TextColor.OnSurface,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->

        ObserveAsEvents(viewModel.navigator.navigationActions) { action ->
            when (action) {
                is NavigationAction.Navigate -> {
                    navController.navigate(action.destination) {
                        action.navOptions(this)
                        val currentRouteString =
                            navController.currentBackStackEntry?.destination?.route
                        val startDestinationRouteString =
                            LoginScreenState.Loading::class.qualifiedName
                        if (currentRouteString != null && currentRouteString == startDestinationRouteString) {
                            popUpTo(LoginScreenState.Loading::class) { inclusive = true }
                        }
                    }
                }

                NavigationAction.NavigateUp -> navController.navigateUp()
                NavigationAction.NavigateToHome -> onNavigateToHome()
                NavigationAction.NavigateToSync -> onNavigateToSync()
                NavigationAction.NavigateToPrivacyPolicy -> onNavigateToPrivacyPolicy()
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
                CredentialsScreen(
                    selectedServer = arg.selectedServer,
                    selectedServerName = arg.serverName,
                    selectedUsername = arg.selectedUsername?.takeIf { username -> username.isNotEmpty() },
                    allowRecovery = arg.allowRecovery,
                )
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
            composable<LoginScreenState.RecoverAccount> {
                val arg = it.toRoute<LoginScreenState.RecoverAccount>()
                onNavigateToRecoverAccount(arg.selectedServer)
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

                    val items =
                        listOf(
                            MenuItemData(
                                id = 1,
                                label = stringResource(Res.string.import_database),
                                leadingElement =
                                    MenuLeadingElement.Icon(
                                        icon = Icons.Filled.FileUpload,
                                    ),
                            ),
                        )

                    DropDownMenu(
                        items = items,
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        expanded = false
                        onImportDatabase()
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
