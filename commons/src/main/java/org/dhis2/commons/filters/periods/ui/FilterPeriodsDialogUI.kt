package org.dhis2.commons.filters.periods.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.collectAsLazyPagingItems
import org.dhis2.commons.filters.periods.model.FilterPeriodType
import org.dhis2.commons.filters.periods.ui.FilterPeriodsDialog.FilterDialogLaunchMode
import org.dhis2.commons.filters.periods.ui.viewmodel.FilterPeriodsDialogViewmodel
import org.dhis2.commons.periods.ui.PeriodSelectorContent
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.DatePicker
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownListItem
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing.Spacing8
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPeriodsDialogUI(
    viewModel: FilterPeriodsDialogViewmodel,
    launchMode: FilterDialogLaunchMode,
    onDismiss: () -> Unit,
) {
    val screenState by viewModel.filterPeriodsScreenState.collectAsState()
    var showDatePicker by remember(screenState.showDatePicker) { mutableStateOf(launchMode.isFromToFilter || screenState.showDatePicker) }
    var showToDatePicker by remember { mutableStateOf(false) }
    val fromDatePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = Date().time,
        )

    val toDatePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = Date().time,
        )

    AnimatedVisibility(
        visible = (showDatePicker),
        enter =
            fadeIn(
                animationSpec = tween(durationMillis = 400),
            ),
        exit =
            fadeOut(
                animationSpec = tween(durationMillis = 400),
            ),
    ) {
        DatePicker(
            title = "Select Date",
            state = fromDatePickerState,
            acceptText = "OK",
            cancelText = "Cancel",
            onCancel = { onDismiss() },
            onConfirm = {
                if (launchMode.isFromToFilter) {
                    showDatePicker = false
                    showToDatePicker = true
                } else {
                    viewModel.setDailyPeriodFilter(fromDatePickerState.selectedDateMillis)
                    onDismiss()
                }
            },
            onDismissRequest = { onDismiss() },
            modifier = Modifier.wrapContentSize(),
        )
    }

    AnimatedVisibility(
        visible = showToDatePicker,
        enter =
            fadeIn(
                animationSpec = tween(durationMillis = 400),
            ),
        exit =
            fadeOut(
                animationSpec = tween(durationMillis = 400),
            ),
    ) {
        DatePicker(
            title = "Select Date",
            state = toDatePickerState,
            acceptText = "OK",
            cancelText = "Cancel",
            onCancel = { onDismiss() },
            onConfirm = {
                viewModel.setFromToFilter(
                    fromDatePickerState.selectedDateMillis,
                    toDatePickerState.selectedDateMillis,
                )
                onDismiss()
            },
            onDismissRequest = { onDismiss() },
            modifier = Modifier.wrapContentSize(),
        )
    }

    AnimatedVisibility(
        visible = (!showDatePicker && !launchMode.isFromToFilter),
        enter =
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 500),
            ),
        exit =
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 500),
            ),
    ) {
        val scrollState = rememberLazyListState()

        BottomSheetShell(
            uiState =
                BottomSheetShellUIState(
                    showTopSectionDivider = true,
                    showBottomSectionDivider = true,
                    title = screenState.title,
                    headerTextAlignment = TextAlign.Center,
                    animateHeaderOnKeyboardAppearance = false,
                ),
            contentScrollState = scrollState,
            content = {
                when {
                    (screenState.selectedPeriodType == null) -> {
                        val periodTypes = screenState.periodTypes
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            state = scrollState,
                        ) {
                            items(periodTypes.count()) { index ->
                                DropdownListItem(
                                    item =
                                        DropdownItem(
                                            label = viewModel.getPeriodTypeName(periodTypes[index]),
                                        ),
                                    contentPadding = PaddingValues(Spacing8),
                                    selected = false,
                                    enabled = true,
                                    onItemClick = {
                                        viewModel.onPeriodTypeSelected(
                                            periodTypes[index],
                                        )
                                    },
                                )
                            }
                        }
                    }

                    else -> {
                        if (screenState.selectedPeriodType != FilterPeriodType.DAILY) {
                            val periods =
                                viewModel
                                    .fetchPeriods()
                                    .collectAsLazyPagingItems()
                            PeriodSelectorContent(
                                periods = periods,
                                scrollState = scrollState,
                            ) { period ->
                                viewModel.onPeriodSelected(period)
                                onDismiss()
                            }
                        }
                    }
                }
            },
            onDismiss = onDismiss,
        )
    }
}
