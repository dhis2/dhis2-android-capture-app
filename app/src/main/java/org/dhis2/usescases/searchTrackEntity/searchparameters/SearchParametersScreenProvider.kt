package org.dhis2.usescases.searchTrackEntity.searchparameters

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import com.journeyapps.barcodescanner.ScanOptions
import org.dhis2.commons.Constants
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.data.scan.ScanContract
import org.dhis2.form.ui.customintent.CustomIntentActivityResultContract
import org.dhis2.form.ui.customintent.CustomIntentInput
import org.dhis2.mobile.commons.extensions.ObserveAsEvents
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.dhis2.tracker.search.ui.provider.SearchParametersScreen
import org.dhis2.tracker.search.ui.provider.SearchScreenUiEvent
import org.dhis2.tracker.ui.input.action.TrackerInputAction
import org.dhis2.tracker.ui.input.model.TrackerInputUiEvent
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel

fun provideSearchScreen(
    composeView: ComposeView,
    viewModel: SearchTEIViewModel,
    program: String?,
    teiType: String,
    resources: ResourceManager,
    onShowOrgUnit: (
        uid: String,
        preselectedOrgUnits: List<String>,
        orgUnitScope: OrgUnitSelectorScope,
        label: String,
    ) -> Unit,
    onClear: () -> Unit,
) {
    viewModel.fetchSearchParameters(
        programUid = program,
        teiTypeUid = teiType,
    )
    composeView.setContent {
        val customIntentlauncher =
            rememberLauncherForActivityResult(
                contract = CustomIntentActivityResultContract(),
                onResult = viewModel::handleCustomIntentResult,
            )

        val scanContract = remember { ScanContract() }

        val qrScanLauncher =
            rememberLauncherForActivityResult(
                contract = scanContract,
            ) { result ->
                result.contents?.let { qrData ->
                    viewModel.handleScanResult(
                        fieldUid = result.originalIntent.getStringExtra(Constants.UID)!!,
                        value = qrData,
                    )
                }
            }

        ObserveAsEvents(
            flow = viewModel.searchActions,
        ) { action ->
            when (action) {
                is TrackerInputAction.LaunchCustomIntent -> {
                    customIntentlauncher.launch(
                        with(action) {
                            CustomIntentInput(
                                fieldUid = fieldUid,
                                customIntent = customIntentModel,
                                defaultTitle =
                                    customIntentModel.name
                                        ?: resources.getString(R.string.select_app_intent),
                            )
                        },
                    )
                }

                is TrackerInputAction.Scan -> {
                    with(action) {
                        qrScanLauncher.launch(
                            ScanOptions().apply {
                                setDesiredBarcodeFormats()
                                setPrompt("")
                                setBeepEnabled(true)
                                setBarcodeImageEnabled(false)
                                addExtra(Constants.UID, fieldUid)
                                optionSet?.let {
                                    addExtra(
                                        Constants.OPTION_SET,
                                        it,
                                    )
                                }
                                addExtra(
                                    Constants.SCAN_RENDERING_TYPE,
                                    renderType,
                                )
                            },
                        )
                    }
                }

                is TrackerInputAction.ValueChanged -> {
                    viewModel.onValueChange(
                        fieldUid = action.fieldUid,
                        value = action.value,
                    )
                }
            }
        }

        val configuration = LocalConfiguration.current

        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        SearchParametersScreen(
            uiState = viewModel.searchParametersUiState,
            onSearchScreenUiEvent = {
                when (it) {
                    is SearchScreenUiEvent.OnSearchButtonClicked ->
                        viewModel.onSearch()

                    is SearchScreenUiEvent.OnClearSearchButtonClicked -> {
                        onClear()
                        viewModel.clearQueryData()
                        viewModel.clearFocus()
                    }

                    is SearchScreenUiEvent.OnCloseClicked -> viewModel.clearFocus()
                }
            },
            isLandscape = isLandscape,
            onTrackerInputUiEvent = {
                when (it) {
                    is TrackerInputUiEvent.OnScanButtonClicked ->
                        viewModel.launchScan(
                            it.uid,
                            it.optionSet,
                            it.renderType,
                        )

                    is TrackerInputUiEvent.OnOrgUnitButtonClicked ->
                        onShowOrgUnit(
                            it.uid,
                            it.value?.let { listOf(it) }
                                ?: emptyList(),
                            it.orgUnitSelectorScope
                                ?: OrgUnitSelectorScope.UserSearchScope(),
                            it.label,
                        )

                    is TrackerInputUiEvent.OnLaunchCustomIntent ->
                        viewModel.launchCustomIntent(
                            it.uid,
                            it.customIntentUid,
                        )

                    is TrackerInputUiEvent.OnItemClick -> viewModel.onItemClick(it.uid)

                    is TrackerInputUiEvent.OnValueChange ->
                        viewModel.onValueChange(
                            fieldUid = it.uid,
                            value = it.value,
                        )
                }
            },
            getOptionSetFlow = { fieldUid, optionSetUid ->
                viewModel.getOptionSetFlow(fieldUid, optionSetUid)
            },
            onOptionSetSearch = { fieldUid, query ->
                viewModel.onOptionSetSearch(fieldUid, query)
            },
        )
    }
}
