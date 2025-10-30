package org.dhis2.usescases.datasets.datasetInitial.periods.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.commons.R
import org.dhis2.commons.date.toUiStringResource
import org.dhis2.commons.periods.ui.PeriodSelectorContent
import org.dhis2.usescases.datasets.datasetInitial.periods.DatasetPeriodViewModel
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.DatePicker
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar
import java.util.Date

class DataSetPeriodDialog(
    private val dataset: String,
    private val periodType: PeriodType,
    private val selectedDate: Date?,
    private val openFuturePeriods: Int,
) : BottomSheetDialogFragment() {
    lateinit var onDateSelectedListener: (Date, String) -> Unit

    val viewModel by viewModel<DatasetPeriodViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                DHIS2Theme {
                    if (periodType == PeriodType.Daily &&
                        !viewModel.verifyIfHasDataInputPeriods(dataset)
                    ) {
                        val calendar = Calendar.getInstance()
                        calendar.time =
                            viewModel.getPeriodMaxDate(periodType, openFuturePeriods + 1)

                        val state =
                            rememberDatePickerState(
                                initialSelectedDateMillis = selectedDate?.time ?: Date().time,
                                yearRange = 1970..calendar[Calendar.YEAR],
                                selectableDates =
                                    object : SelectableDates {
                                        override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= calendar.timeInMillis

                                        override fun isSelectableYear(year: Int): Boolean = year <= calendar[Calendar.YEAR]
                                    },
                            )

                        DatePicker(
                            title = getString(periodType.toUiStringResource()),
                            state = state,
                            acceptText = getString(R.string.accept),
                            cancelText = getString(R.string.cancel),
                            onCancel = { dismiss() },
                            onConfirm = { date ->
                                date.selectedDateMillis?.let {
                                    onDateSelectedListener(Date(it), "")
                                }
                                dismiss()
                            },
                            onDismissRequest = { dismiss() },
                        )
                    } else {
                        val scrollState = rememberLazyListState()

                        // If future period entry is enabled we scroll after opening the dialog to
                        // ensure data entry starts with "today's" period instead of the latest
                        // period. The user can scroll up to move into the future.
                        LaunchedEffect(openFuturePeriods) {
                            if (openFuturePeriods > 0) {
                                scrollState.animateScrollToItem(openFuturePeriods - 1)
                            }
                        }

                        BottomSheetShell(
                            uiState =
                                BottomSheetShellUIState(
                                    title = getString(periodType.toUiStringResource()),
                                    showTopSectionDivider = true,
                                ),
                            onDismiss = { dismiss() },
                            contentScrollState = scrollState,
                            content = {
                                val periods =
                                    viewModel
                                        .fetchPeriods(
                                            dataset,
                                            periodType,
                                            selectedDate,
                                            openFuturePeriods,
                                        ).collectAsLazyPagingItems()
                                PeriodSelectorContent(
                                    periods = periods,
                                    scrollState = scrollState,
                                ) { period ->
                                    onDateSelectedListener(period.startDate, period.name)
                                    dismiss()
                                }
                            },
                        )
                    }
                }
            }
        }
}
