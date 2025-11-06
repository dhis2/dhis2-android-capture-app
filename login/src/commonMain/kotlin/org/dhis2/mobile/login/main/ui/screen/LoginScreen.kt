package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil3.compose.LocalPlatformContext
import org.dhis2.mobile.commons.extensions.ObserveAsEvents
import org.dhis2.mobile.login.accounts.ui.screen.AccountsScreen
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.ui.contracts.filePicker
import org.dhis2.mobile.login.main.ui.navigation.NavigationAction
import org.dhis2.mobile.login.main.ui.state.DatabaseImportState
import org.dhis2.mobile.login.main.ui.state.OidcInfo
import org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModel
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.ic_dhis_logo
import org.dhis2.mobile.login.resources.import_database
import org.dhis2.mobile.login.resources.importing_successful
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.menu.DropDownMenu
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuLeadingElement
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun LoginScreen(
    navController: NavHostController = rememberNavController(),
    versionName: String,
    fromHome: Boolean,
    onNavigateToSync: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onFinish: () -> Unit,
) {
    val context = LocalPlatformContext.current
    val viewModel = koinViewModel<LoginViewModel> { parametersOf(context) }
    var displayMoreActions by remember { mutableStateOf(false) }
    var displayBackArrow by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val databaseImportState by viewModel.importDatabaseState.collectAsState()

    val picker =
        filePicker { path ->
            path?.let { viewModel.importDb(it) }
        }

    LaunchedEffect(databaseImportState) {
        databaseImportState?.let { state ->
            when (state) {
                is DatabaseImportState.OnFailure -> {
                    snackBarHostState.showSnackbar(state.message)
                }

                is DatabaseImportState.OnSuccess -> {
                    snackBarHostState.showSnackbar(getString(Res.string.importing_successful))
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LoginTopBar(
                version = versionName,
                displayMoreActions = displayMoreActions,
                displayBackArrow = displayBackArrow,
                onBack = {
                    if (!navController.popBackStack()) {
                        onFinish()
                    }
                },
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
                    navController.popBackStack(LoginScreenState.Loading, true)
                    navController.navigate(action.destination)
                }

                NavigationAction.NavigateUp -> navController.navigateUp()
                NavigationAction.NavigateToHome -> onNavigateToHome()
                NavigationAction.NavigateToSync -> onNavigateToSync()
                NavigationAction.NavigateToPrivacyPolicy -> onNavigateToPrivacyPolicy()
            }
        }

        val layoutDirection = LocalLayoutDirection.current

        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = LoginScreenState.Loading,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
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
                    displayBackArrow = false
                    LoadingScreen()
                }
                composable<LoginScreenState.ServerValidation> {
                    val args = it.toRoute<LoginScreenState.ServerValidation>()
                    displayMoreActions = true
                    displayBackArrow = false

                    val uiState by viewModel.serverValidationState.collectAsState()
                    ServerValidationContent(
                        availableServers = args.availableServers,
                        state = uiState,
                        hasAccount = args.hasAccounts,
                        onValidate = viewModel::onValidateServer,
                        onCancel = viewModel::cancelServerValidation,
                        onManageAccounts = viewModel::onBackToManageAccounts,
                    )
                }
                composable<LoginScreenState.LegacyLogin> {
                    val arg = it.toRoute<LoginScreenState.LegacyLogin>()
                    displayMoreActions = arg.selectedServer.isEmpty()
                    displayBackArrow = true
                    CredentialsScreen(
                        selectedServer = arg.selectedServer,
                        selectedServerName = arg.serverName,
                        selectedUsername = arg.selectedUsername?.takeIf { username -> username.isNotEmpty() },
                        selectedServerFlag = arg.selectedServerFlag,
                        allowRecovery = arg.allowRecovery,
                        oidcInfo =
                            fixedOpenIdProvider()?.takeIf { info ->
                                info.serverUrl == arg.selectedServer
                            },
                        fromHome = fromHome,
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
                    displayBackArrow = false
                    AccountsScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                    )
                }
                composable<LoginScreenState.RecoverAccount> {
                    val arg = it.toRoute<LoginScreenState.RecoverAccount>()
                    WebRecovery(arg.selectedServer) {
                        viewModel.onRecoveryCancelled()
                    }
                }
            }
        }
    }
}

/**
 * OpenId Configuration
 * Return either OidcInfo.Token or OidcInfo.Discovery classes to configure the login screen.
 * Don't forget to add the RedirectUriReceiverActivity in the android manifest. Check the
 * documentation for more info.
 * */
private fun fixedOpenIdProvider(): OidcInfo? {
    // Change to the correct provider
    return null
}

@Composable
fun LoginTopBar(
    version: String,
    displayMoreActions: Boolean = true,
    displayBackArrow: Boolean = true,
    onBack: () -> Unit,
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
                    .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            if (displayBackArrow) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = onBack,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "back",
                        tint = MaterialTheme.colorScheme.surfaceBright,
                    )
                }
            }
            Image(
                modifier =
                    Modifier
                        .padding(8.dp)
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

@Preview
@Composable
fun TopBarPreview() {
    DHIS2Theme {
        LoginTopBar("v3.3.0-DEV : 0b3f5487", true, onBack = {}) {}
    }
}
