package org.dhis2.usescases.development

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import kotlin.js.ExperimentalJsExport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.dhis2.usescases.general.ActivityGlobalAbstract
import period_calculation.CalendarType
import period_calculation.FixedPeriod
import period_calculation.PeriodGenerator
import period_calculation.PeriodOptions
import period_calculation.PeriodType

@OptIn(ExperimentalJsExport::class)
class PeriodGenerationActivity : ActivityGlobalAbstract() {
    private val viewModel: PeriodGenerationViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Mdc3Theme {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val currentValue by viewModel.screenState.collectAsState()
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = currentValue.currentValue,
                        onValueChange = viewModel::updateValue
                    )
                    ExposedDropdownMenuBox(
                        expanded = currentValue.showDropDown,
                        onExpandedChange = { viewModel.showDropDown() }
                    ) {
                        TextField(
                            value = when(currentValue.calendarType){
                                is CalendarType.Ethiopian -> "Ethiopian"
                                is CalendarType.Gregorian -> "Gregorian"
                                is CalendarType.Nepali -> "Nepali"
                            },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currentValue.showDropDown) },
                            modifier = Modifier
                        )

                        DropdownMenu(expanded = currentValue.showDropDown, onDismissRequest = { }) {
                            DropdownMenuItem(onClick = { viewModel.selectCalendar(CalendarType.Gregorian()) }) {
                                Text(text = "Gregorian")

                            }
                            DropdownMenuItem(onClick = { viewModel.selectCalendar(CalendarType.Ethiopian()) }) {
                                Text(text = "Ethiopian")

                            }
                            DropdownMenuItem(onClick = { viewModel.selectCalendar(CalendarType.Nepali()) }) {
                                Text(text = "Nepali")

                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = spacedBy(8.dp)
                    ) {
                        items(items = currentValue.periods) { fixedPeriod ->
                            Card(Modifier.fillMaxWidth().padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            )) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    verticalArrangement = spacedBy(4.dp)
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = "PeriodId: ${fixedPeriod.id}",
                                    )
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = fixedPeriod.name
                                    )
                                }
                            }
                        }

                    }

                    Button(onClick = viewModel::getPeriods) {
                        Text(text = "Load periods")
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalJsExport::class)
class PeriodGenerationViewModel : ViewModel() {
    private val periodGenerator = PeriodGenerator()
    private val _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> get() = _screenState

    fun updateValue(newValue: String) {
        viewModelScope.launch {
            _screenState.emit(_screenState.value.copy(currentValue = newValue))
        }
    }

    fun getPeriods() {
        viewModelScope.launch {
            val periods = periodGenerator.generatePeriod(
                PeriodOptions(
                    year = _screenState.value.currentValue.toIntOrNull() ?: 2023,
                    periodType = PeriodType.DAILY,
                    calendar = _screenState.value.calendarType
                )
            )
            _screenState.emit(_screenState.value.copy(periods = periods.toList()))
        }
    }

    fun selectCalendar(calendar: CalendarType) {
        viewModelScope.launch {
            _screenState.emit(with(_screenState.value) {
                copy(
                    calendarType = calendar,
                    showDropDown = false,
                    periods = emptyList()
                )
            })
        }
    }

    fun showDropDown() {
        viewModelScope.launch {
            _screenState.emit(with(_screenState.value) {
                copy(showDropDown = !showDropDown)
            })
        }
    }
}

@OptIn(ExperimentalJsExport::class)
data class ScreenState(
    val currentValue: String = "",
    val periods: List<FixedPeriod> = emptyList(),
    val calendarType: CalendarType = CalendarType.Gregorian(),
    val showDropDown: Boolean = false,
)