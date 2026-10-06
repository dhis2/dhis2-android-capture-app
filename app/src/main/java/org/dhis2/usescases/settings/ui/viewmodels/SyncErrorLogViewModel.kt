package org.dhis2.usescases.settings.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.usescases.settings.domain.GetSharedData
import org.dhis2.usescases.settings.domain.GetSyncErrors
import org.dhis2.usescases.settings.models.SyncErrorLogUiState

class SyncErrorLogViewModel(
    private val getSyncErrors: GetSyncErrors,
    private val getSharedData: GetSharedData,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {
    private val _errorLogUiState: MutableStateFlow<SyncErrorLogUiState> =
        MutableStateFlow(SyncErrorLogUiState.Loading)
    private val _shareEvent = MutableSharedFlow<String>()
    val shareEvent = _shareEvent.asSharedFlow()

    val errorLogUiState =
        _errorLogUiState
            .onStart {
                loadErrorLog()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SyncErrorLogUiState.Loading,
            )

    private fun loadErrorLog() {
        launchUseCase(dispatcher = dispatchers.io()) {
            val result = getSyncErrors()
            _errorLogUiState.update {
                SyncErrorLogUiState.Log(
                    errorList = result.getOrDefault(emptyList()),
                )
            }
        }
    }

    fun initSelectionMode() {
        launchUseCase(dispatchers.io()) {
            _errorLogUiState.update {
                SyncErrorLogUiState.Selection(errorList = (it as SyncErrorLogUiState.Log).errorList)
            }
        }
    }

    fun exitSelectionMode() {
        launchUseCase(dispatchers.io()) {
            _errorLogUiState.update {
                SyncErrorLogUiState.Log(
                    errorList =
                        (it as SyncErrorLogUiState.Selection).errorList.map { errorModel ->
                            errorModel.copy(isSelected = false)
                        },
                )
            }
        }
    }

    fun setSelected(selectedIndex: Int) {
        launchUseCase(dispatchers.io()) {
            _errorLogUiState.update {
                val updatedList =
                    (it as SyncErrorLogUiState.Selection).errorList.mapIndexed { index, model ->
                        if (index == selectedIndex) {
                            model.copy(isSelected = !model.isSelected)
                        } else {
                            model
                        }
                    }
                SyncErrorLogUiState.Selection(errorList = updatedList)
            }
        }
    }

    fun onShareLog() {
        launchUseCase(dispatchers.io()) {
            getSharedData((errorLogUiState.value as SyncErrorLogUiState.Selection).errorList)
                .onSuccess {
                    _shareEvent.emit(it)
                }
        }
    }
}
