package org.dhis2.android.rtsm.ui.home.screens.components

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.home.HomeActivity
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.model.DataEntryStep
import org.dhis2.android.rtsm.ui.home.model.DataEntryUiState
import org.dhis2.android.rtsm.ui.home.model.EditionDialogResult
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Backdrop(
    activity: Activity,
    viewModel: HomeViewModel,
    manageStockViewModel: ManageStockViewModel,
    themeColor: Color,
    supportFragmentManager: FragmentManager,
    barcodeLauncher: ActivityResultLauncher<ScanOptions>,
    scaffoldState: ScaffoldState,
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> }
) {
    val backdropState = rememberBackdropScaffoldState(BackdropValue.Revealed)
    var isFrontLayerDisabled by remember { mutableStateOf<Boolean?>(null) }
    val settingsUiState by viewModel.settingsUiState.collectAsState()
    val dataEntryUiState by manageStockViewModel.dataEntryUiState.collectAsState()

    (activity as HomeActivity).onBackPressed = {
        handleBackNavigation(
            activity,
            dataEntryUiState,
            supportFragmentManager,
            manageStockViewModel
        )
    }

    BackdropScaffold(
        appBar = {
            Toolbar(
                settingsUiState.transactionType.name,
                settingsUiState.fromFacilitiesLabel().asString(),
                settingsUiState.deliverToLabel()?.asString(),
                themeColor,
                launchBottomSheet = {
                    if (dataEntryUiState.hasUnsavedData) {
                        launchBottomSheet(
                            activity.getString(R.string.not_saved),
                            activity.getString(R.string.transaction_not_confirmed),
                            supportFragmentManager,
                            onKeepEdition = { },
                            onDiscard = {
                                activity.finish()
                            }
                        )
                    } else {
                        activity.finish()
                    }
                },
                backdropState,
                scaffoldState,
                syncAction,
                settingsUiState.hasFacilitySelected(),
                settingsUiState.hasDestinationSelected()
            )
        },
        backLayerBackgroundColor = themeColor,
        backLayerContent = {
            FilterList(
                viewModel,
                dataEntryUiState.hasUnsavedData,
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
                        }
                    )
                },
                onTransitionSelected = {
                    viewModel.selectTransaction(it)
                },
                onFacilitySelected = {
                    viewModel.setFacility(it)
                },
                onDestinationSelected = {
                    viewModel.setDestination(it)
                }
            )
        },
        frontLayerElevation = 5.dp,
        frontLayerContent = {
            MainContent(
                backdropState,
                isFrontLayerDisabled,
                themeColor,
                viewModel,
                manageStockViewModel,
                barcodeLauncher
            )
        },
        scaffoldState = backdropState,
        gesturesEnabled = false,
        frontLayerScrimColor = if (
            settingsUiState.transactionType == TransactionType.DISTRIBUTION
        ) {
            if (settingsUiState.hasFacilitySelected() && settingsUiState.hasDestinationSelected()) {
                isFrontLayerDisabled = false
                Color.Unspecified
            } else {
                isFrontLayerDisabled = true
                MaterialTheme.colors.surface.copy(alpha = 0.60f)
            }
        } else {
            if (!settingsUiState.hasFacilitySelected()) {
                isFrontLayerDisabled = true
                MaterialTheme.colors.surface.copy(alpha = 0.60f)
            } else {
                isFrontLayerDisabled = false
                Color.Unspecified
            }
        }
    )
}

fun handleBackNavigation(
    activity: HomeActivity,
    dataEntryUiState: DataEntryUiState,
    supportFragmentManager: FragmentManager,
    viewModel: ManageStockViewModel
) {
    when (dataEntryUiState.step) {
        DataEntryStep.LISTING -> {
            if (dataEntryUiState.hasUnsavedData) {
                launchBottomSheet(
                    activity.getString(R.string.not_saved),
                    activity.getString(R.string.transaction_not_confirmed),
                    supportFragmentManager,
                    onKeepEdition = { },
                    onDiscard = {
                        activity.finish()
                    }
                )
            } else {
                activity.finish()
            }
        }
        DataEntryStep.REVIEWING -> viewModel.onHandleBackNavigation()
        else -> {
            // TODO Implement next steps back behaviour
        }
    }
}

private fun launchBottomSheet(
    title: String,
    subtitle: String,
    supportFragmentManager: FragmentManager,
    onDiscard: () -> Unit, // Perform the transaction change and clear data
    onKeepEdition: () -> Unit // Leave it as it was
) {
    BottomSheetDialog(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = title,
            subtitle = subtitle,
            iconResource = R.drawable.ic_outline_error_36,
            mainButton = DialogButtonStyle.MainButton(org.dhis2.commons.R.string.keep_editing),
            secondaryButton = DialogButtonStyle.DiscardButton()
        ),
        onMainButtonClicked = {
            supportFragmentManager.popBackStack()
            onKeepEdition.invoke()
        },
        onSecondaryButtonClicked = { onDiscard.invoke() }
    ).apply {
        this.show(supportFragmentManager.beginTransaction(), "DIALOG")
    }
}
