package org.dhis2.usescases.programEventDetail

import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import org.dhis2.R
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.databinding.ActivityProgramEventDetailBinding
import org.dhis2.model.SnackbarMessage
import org.dhis2.usescases.programEventDetail.ProgramEventDetailViewModel.EventProgramScreen
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.hisp.dhis.mobile.ui.designsystem.component.FAB
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow

@Composable
fun ProgramEventDetailScreen(
    programEventsViewModel: ProgramEventDetailViewModel,
    presenter: ProgramEventDetailPresenter,
    networkUtils: NetworkUtils,
    onBindingReady: (ActivityProgramEventDetailBinding) -> Unit,
    onViewReady: () -> Unit,
) {
    val context = LocalContext.current
    val isBackdropActive by programEventsViewModel.backdropActive.observeAsState(false)
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by programEventsViewModel.snackbarMessage.collectAsState(SnackbarMessage())

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.message.isNotEmpty()) {
            snackbarHostState.showSnackbar(snackbarMessage.message)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.dropShadow(shape = SnackbarDefaults.shape),
                    snackbarData = data,
                    containerColor = SurfaceColor.SurfaceBright,
                    contentColor = TextColor.OnSurface,
                )
            }
        },
        floatingActionButton = {
            val writePermission by programEventsViewModel.writePermission.observeAsState(
                false,
            )
            val currentScreen by programEventsViewModel.currentScreen.observeAsState()
            val displayFAB by remember {
                derivedStateOf {
                    when (currentScreen) {
                        EventProgramScreen.LIST -> true
                        else -> false
                    } &&
                        writePermission &&
                        isBackdropActive.not()
                }
            }
            AnimatedVisibility(
                visible = displayFAB,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FAB(
                    modifier = Modifier.testTag("ADD_EVENT_BUTTON"),
                    onClick = presenter::addEvent,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "add event",
                            tint = TextColor.OnPrimary,
                        )
                    },
                )
            }
        },
        bottomBar = {
            val uiState by programEventsViewModel.navigationBarUIState

            var selectedItemIndex by remember(uiState) {
                mutableIntStateOf(
                    uiState.items.indexOfFirst {
                        it.id == uiState.selectedItem
                    },
                )
            }

            LaunchedEffect(uiState.selectedItem) {
                when (uiState.selectedItem) {
                    NavigationPage.LIST_VIEW -> {
                        programEventsViewModel.showList()
                    }

                    NavigationPage.MAP_VIEW -> {
                        networkUtils.performIfOnline(
                            context = context,
                            action = {
                                presenter.trackEventProgramMap()
                                programEventsViewModel.showMap()
                            },
                            onDialogDismissed = {
                                selectedItemIndex = 0
                            },
                            noNetworkMessage = context.getString(R.string.msg_network_connection_maps),
                        )
                    }

                    NavigationPage.ANALYTICS -> {
                        presenter.trackEventProgramAnalytics()
                        programEventsViewModel.showAnalytics()
                    }

                    else -> {
                        // no-op
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.items.size > 1 && isBackdropActive.not(),
                enter = slideInVertically(animationSpec = tween(200)) { it },
                exit = slideOutVertically(animationSpec = tween(200)) { it },
            ) {
                NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    items = uiState.items,
                    selectedItemIndex = selectedItemIndex,
                    onItemClick = programEventsViewModel::onNavigationPageChanged,
                )
            }
        },
    ) {
        AndroidView(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(it),
            factory = { context ->
                ActivityProgramEventDetailBinding
                    .inflate(
                        LayoutInflater.from(context),
                    ).also(onBindingReady)
                    .root
            },
            update = {
                onViewReady()
                presenter.init()
            },
        )
    }
}
