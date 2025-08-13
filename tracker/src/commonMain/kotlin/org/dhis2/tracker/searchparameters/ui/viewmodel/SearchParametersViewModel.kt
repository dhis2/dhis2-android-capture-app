package org.dhis2.tracker.searchparameters.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.mobile.commons.input.UiAction
import org.dhis2.mobile.commons.input.UiActionHandler
import org.dhis2.tracker.searchparameters.ui.state.SearchUiState

class SearchParametersViewModel(
    private val uiActionHandler: UiActionHandler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onUiAction(uiAction: UiAction) {
        viewModelScope.launch {
            when (uiAction) {
                is UiAction.OnOpenOrgUnitTree -> {
                    uiActionHandler.onCaptureOrgUnit(
                        preselectedOrgUnits = uiAction.currentOrgUnitUid?.let {
                            listOf(it)
                        } ?: emptyList(),
                    ) {
                        TODO("Implement org unit tree handling")
                    }
                }

                is UiAction.OnBarCodeScan -> {
                    uiActionHandler.onBarcodeScan(
                        fieldUid = uiAction.id,
                        optionSet = uiAction.optionSet,
                    ) {
                        TODO("Implement barcode scan handling")
                    }
                }

                is UiAction.OnDisplayBarCode -> {
                    uiActionHandler.onDisplayBarCode(
                        fieldUid = uiAction.id,
                        value = uiAction.value,
                        label = uiAction.label,
                        editable = uiAction.editable,
                    ) {
                        uiActionHandler.onBarcodeScan(
                            fieldUid = uiAction.id,
                            optionSet = uiAction.optionSet,
                        ) {
                            TODO("Implement barcode scan handling")
                        }
                    }
                }

                is UiAction.OnQRCodeScan -> {
                    uiActionHandler.onQRScan(
                        fieldUid = uiAction.id,
                        optionSet = uiAction.optionSet,
                    ) {
                        TODO("Implement QR code scan handling")
                    }
                }

                is UiAction.OnDisplayQRCode -> {
                    uiActionHandler.onDisplayQRCode(
                        fieldUid = uiAction.id,
                        value = uiAction.value,
                        label = uiAction.label,
                        editable = uiAction.editable,
                    ) {
                        uiActionHandler.onQRScan(
                            fieldUid = uiAction.id,
                            optionSet = uiAction.optionSet,
                        ) {
                            TODO("Implement QR code scan handling")
                        }
                    }
                }

                is UiAction.OnAddImage -> TODO()
                is UiAction.OnCall -> TODO()
                is UiAction.OnCaptureCoordinates -> TODO()
                is UiAction.OnDoneClick -> TODO()
                is UiAction.OnDownloadFile -> TODO()
                is UiAction.OnEmailAction -> TODO()
                is UiAction.OnFetchOptions -> TODO()
                is UiAction.OnFocusChanged -> TODO()
                is UiAction.OnLinkClicked -> TODO()
                is UiAction.OnNextClick -> TODO()
                is UiAction.OnSelectFile -> TODO()
                is UiAction.OnShareImage -> TODO()
                is UiAction.OnTakePhoto -> TODO()
                is UiAction.OnValueChanged -> TODO()
            }
        }
    }
}
