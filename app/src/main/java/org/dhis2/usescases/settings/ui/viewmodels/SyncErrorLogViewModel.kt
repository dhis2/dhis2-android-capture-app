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
        _errorLogUiState.update { state ->
            when (state) {
                is SyncErrorLogUiState.Log -> SyncErrorLogUiState.Selection(errorList = state.errorList)
                else -> state
            }
        }
    }

    fun exitSelectionMode() {
        _errorLogUiState.update { state ->
            when (state) {
                is SyncErrorLogUiState.Selection ->
                    SyncErrorLogUiState.Log(
                        errorList = state.errorList.map { it.copy(isSelected = false) },
                    )

                else -> state
            }
        }
    }

    /**
     * Toggles the item at [selectedIndex]. The card fires this on long press even outside
     * selection mode, so from [SyncErrorLogUiState.Log] it enters selection mode with the
     * item selected.
     */
    fun setSelected(selectedIndex: Int) {
        _errorLogUiState.update { state ->
            val errorList =
                when (state) {
                    is SyncErrorLogUiState.Log -> state.errorList
                    is SyncErrorLogUiState.Selection -> state.errorList
                    SyncErrorLogUiState.Loading -> return@update state
                }
            SyncErrorLogUiState.Selection(
                errorList =
                    errorList.mapIndexed { index, model ->
                        if (index == selectedIndex) {
                            model.copy(isSelected = !model.isSelected)
                        } else {
                            model
                        }
                    },
            )
        }
    }

    fun onShareLog() {
        val state = errorLogUiState.value as? SyncErrorLogUiState.Selection ?: return
        launchUseCase(dispatchers.io()) {
            getSharedData(state.errorList)
                .onSuccess {
                    _shareEvent.emit(it)
                }
        }
    }
}
