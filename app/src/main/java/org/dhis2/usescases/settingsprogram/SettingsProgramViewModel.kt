package org.dhis2.usescases.settingsprogram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.dhis2.usescases.settingsprogram.domain.GetProgramSpecificSettings
import org.dhis2.usescases.settingsprogram.model.SpecificSettings

class SettingsProgramViewModel(
    private val getProgramSpecificSettings: GetProgramSpecificSettings,
) : ViewModel() {
    private val _programSettings = MutableStateFlow(emptyList<SpecificSettings>())
    val programSettings =
        _programSettings
            .onStart {
                loadData()
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                emptyList(),
            )

    private suspend fun loadData() {
        _programSettings.emit(getProgramSpecificSettings())
    }
}
