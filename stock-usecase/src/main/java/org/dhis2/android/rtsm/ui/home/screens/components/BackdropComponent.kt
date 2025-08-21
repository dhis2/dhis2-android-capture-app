package org.dhis2.android.rtsm.ui.home.screens.components

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.model.DataEntryStep
import org.dhis2.android.rtsm.ui.home.model.EditionDialogResult
import org.dhis2.android.rtsm.ui.home.model.SettingsUiState
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle

// Custom BackdropState to replace Material's BackdropScaffoldState
data class BackdropState(
    private val _isRevealed: mutableStateOf<Boolean>
) {
    val isRevealed: Boolean get() = _isRevealed.value
    val isConcealed: Boolean get() = !_isRevealed.value
    
    suspend fun reveal() {
        _isRevealed.value = true
    }
    
    suspend fun conceal() {
        _isRevealed.value = false
    }
}

@Composable
fun rememberBackdropState(initialValue: Boolean = true): BackdropState {
    return remember { BackdropState(mutableStateOf(initialValue)) }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Backdrop(
    activity: Activity,
    viewModel: HomeViewModel,
    manageStockViewModel: ManageStockViewModel,
    modifier: Modifier = Modifier,
    themeColor: Color,
    supportFragmentManager: FragmentManager,
    barcodeLauncher: ActivityResultLauncher<ScanOptions>,
    scaffoldState: ScaffoldState,
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
) {
    val backdropState = rememberBackdropState(initialValue = true)
    var isFrontLayerDisabled by remember { mutableStateOf<Boolean?>(null) }
    val settingsUiState by viewModel.settingsUiState.collectAsState()
    val dataEntryUiState by manageStockViewModel.dataEntryUiState.collectAsState()
    val scope = rememberCoroutineScope()
    val bottomSheetState = manageStockViewModel.bottomSheetState.collectAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    // Animation for backdrop reveal/conceal
    val backdropOffset by animateFloatAsState(
        targetValue = if (backdropState.isRevealed) 0f else -200f,
        label = "backdrop_offset"
    )
    if (bottomSheetState.value) {
        launchBottomSheet(
            activity.getString(R.string.not_saved),
            activity.getString(R.string.transaction_not_confirmed),
            supportFragmentManager,
            onKeepEdition = {
                manageStockViewModel.onBottomSheetClosed()
            },
            onDiscard = {
                manageStockViewModel.onBottomSheetClosed()
                activity.finish()
            },
        )
    }

    BackHandler {
        manageStockViewModel.onHandleBackNavigation()
    }

    manageStockViewModel.backToListing()
    DisplaySnackBar(manageStockViewModel, scaffoldState)
    
    Scaffold(
        modifier = modifier,
        topBar = {
            Toolbar(
                settingsUiState.selectedTransactionItem.label,
                settingsUiState.fromFacilitiesLabel().asString(),
                settingsUiState.deliverToLabel()?.asString(),
                themeColor,
                launchBottomSheet = {
                    manageStockViewModel.onHandleBackNavigation()
                },
                backdropState,
                scaffoldState,
                syncAction,
                settingsUiState.hasFacilitySelected(),
                settingsUiState.hasDestinationSelected(),
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Back layer (Filter List) - positioned behind and slides from top
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(themeColor)
                        .offset(y = backdropOffset.dp)
                ) {
                    FilterList(
                        viewModel,
                        dataEntryUiState,
                        themeColor,
                        supportFragmentManager,
                        launchDialog = { msg, result ->
                            launchBottomSheet(
                                activity.getString(R.string.not_saved),
                                activity.getString(msg),
                                supportFragmentManager,
                                onKeepEdition = {
                                    result.invoke(EditionDialogResult.KEEP)
                                },
                                onDiscard = {
                                    manageStockViewModel.cleanItemsFromCache()
                                    result.invoke(EditionDialogResult.DISCARD)
                                    manageStockViewModel.onHandleBackNavigation()
                                },
                            )
                        },
                        onTransitionSelected = {
                            viewModel.selectTransaction(it)
                        },
                        onFacilitySelected = {
                            viewModel.setFacility(it)
                        },
                    ) {
                        viewModel.setDestination(it)
                    }
                }
                
                // Front layer (Main Content) - positioned on top
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = getScrimColor(settingsUiState).copy(alpha = if (backdropState.isRevealed) 0.3f else 0f),
                        )
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(Color.White)
                        .padding(top = if (backdropState.isRevealed) 200.dp else 0.dp)
                ) {
                    MainContent(
                        backdropState,
                        isFrontLayerDisabled,
                        themeColor,
                        viewModel,
                        manageStockViewModel,
                        barcodeLauncher,
                    )
                }
            }
        }
    )
    isFrontLayerDisabled = getBackdropState(settingsUiState)
    if (dataEntryUiState.step == DataEntryStep.COMPLETED) {
        scope.launch {
            backdropState.reveal()
        }
        manageStockViewModel.updateStep(DataEntryStep.START)
        viewModel.resetSettings()
    }
}

@Composable
private fun getScrimColor(settingsUiState: SettingsUiState): Color {
    return if (settingsUiState.selectedTransactionItem.type == TransactionType.DISTRIBUTION) {
        if (settingsUiState.hasFacilitySelected() && settingsUiState.hasDestinationSelected()) {
            Color.Unspecified
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)
        }
    } else {
        if (!settingsUiState.hasFacilitySelected()) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)
        } else {
            Color.Unspecified
        }
    }
}

@Composable
private fun getBackdropState(settingsUiState: SettingsUiState): Boolean {
    return if (
        settingsUiState.selectedTransactionItem.type == TransactionType.DISTRIBUTION
    ) {
        !(settingsUiState.hasFacilitySelected() && settingsUiState.hasDestinationSelected())
    } else {
        !settingsUiState.hasFacilitySelected()
    }
}

private fun launchBottomSheet(
    title: String,
    subtitle: String,
    supportFragmentManager: FragmentManager,
    onDiscard: () -> Unit, // Perform the transaction change and clear data
    onKeepEdition: () -> Unit, // Leave it as it was
) {
    BottomSheetDialog(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = title,
            message = subtitle,
            iconResource = R.drawable.ic_outline_error_36,
            mainButton = DialogButtonStyle.MainButton(org.dhis2.commons.R.string.keep_editing),
            secondaryButton = DialogButtonStyle.DiscardButton(),
        ),
        onMainButtonClicked = {
            supportFragmentManager.popBackStack()
            onKeepEdition.invoke()
        },
        onSecondaryButtonClicked = { onDiscard.invoke() },
        showTopDivider = true,
    ).apply {
        this.show(supportFragmentManager.beginTransaction(), "DIALOG")
        this.isCancelable = false
    }
}

@Composable
fun DisplaySnackBar(manageStockViewModel: ManageStockViewModel, scaffoldState: ScaffoldState) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        manageStockViewModel.dataEntryUiState.collectLatest {
            if (it.step == DataEntryStep.COMPLETED) {
                coroutineScope.launch {
                    val result = scaffoldState.snackbarHostState.showSnackbar(
                        message = "Snackbar # ",
                        actionLabel = "Action on ",
                        duration = SnackbarDuration.Short,
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            /* action has been performed */
                        }
                        SnackbarResult.Dismissed -> {
                            /* dismissed, no action needed */
                        }
                    }
                }
            }
        }
    }
}
