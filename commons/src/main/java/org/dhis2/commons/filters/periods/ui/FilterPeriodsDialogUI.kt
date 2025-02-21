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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.collectAsLazyPagingItems
import org.dhis2.commons.dialogs.bottomsheet.bottomSheetInsets
import org.dhis2.commons.dialogs.bottomsheet.bottomSheetLowerPadding
import org.dhis2.commons.filters.periods.model.PeriodFilterType
import org.dhis2.commons.filters.periods.ui.state.FilterPeriodsScreenState
import org.dhis2.commons.filters.periods.ui.viewmodel.FilterPeriodsDialogViewmodel
import org.dhis2.commons.periods.ui.ListItem
import org.dhis2.commons.periods.ui.PeriodSelectorContent
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.DatePicker
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing.Spacing8
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPeriodsDialogUI(
    viewModel: FilterPeriodsDialogViewmodel,
    periodFilterType: PeriodFilterType,
    isDataSetPeriodTypes: Boolean,
    isFromToFilter: Boolean,
    onDismiss: () -> Unit,
) {
    viewModel.setFilterType(periodFilterType)
    viewModel.setFilterPeriodTypes(isDataSetPeriodTypes)

    val screenState by viewModel.filterPeriodsScreenState.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val title by viewModel.dialogTitle.collectAsState()
    val fromDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Date().time,
    )

    val toDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Date().time,
    )
    var showToDatePicker by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = (showDatePicker || isFromToFilter),
        enter = fadeIn(
            animationSpec = tween(durationMillis = 400),
        ),
        exit = fadeOut(
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
                if (isFromToFilter) {
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
        enter = fadeIn(
            animationSpec = tween(durationMillis = 400),
        ),
        exit = fadeOut(
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
                viewModel.setFromToFilter(fromDatePickerState.selectedDateMillis, toDatePickerState.selectedDateMillis)
                onDismiss()
            },
            onDismissRequest = { onDismiss() },
            modifier = Modifier.wrapContentSize(),

        )
    }

    AnimatedVisibility(
        visible = (!showDatePicker && !isFromToFilter),
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 500),
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 500),
        ),
    ) {
        val scrollState = rememberLazyListState()

        BottomSheetShell(
            uiState = BottomSheetShellUIState(
                bottomPadding = bottomSheetLowerPadding(),
                showTopSectionDivider = true,
                showBottomSectionDivider = true,
                title = title,
                headerTextAlignment = TextAlign.Center,
                animateHeaderOnKeyboardAppearance = false,
            ),
            windowInsets = { bottomSheetInsets() },
            contentScrollState = scrollState,
            content = {
                when (screenState) {
                    is FilterPeriodsScreenState.Loading -> {
                        ProgressIndicator(type = ProgressIndicatorType.CIRCULAR_SMALL)
                    }

                    is FilterPeriodsScreenState.Loaded -> {
                        when {
                            ((screenState as FilterPeriodsScreenState.Loaded).selectedPeriodType == null) -> {
                                val periodsTypes =
                                    (screenState as FilterPeriodsScreenState.Loaded).periodTypes
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    state = scrollState,
                                ) {
                                    items(periodsTypes.count()) { index ->
                                        ListItem(
                                            contentPadding = PaddingValues(Spacing8),
                                            label = stringResource(periodsTypes[index].nameResource),
                                            selected = false,
                                            enabled = true,
                                            onItemClick = {
                                                viewModel.onPeriodTypeSelected(
                                                    periodsTypes[index],
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                            else -> {
                                val periods =
                                    viewModel.fetchPeriods()
                                        .collectAsLazyPagingItems()
                                PeriodSelectorContent(
                                    periods = periods,
                                    scrollState = scrollState,
                                ) {
                                    viewModel.onPeriodSelected(it)
                                    onDismiss()
                                }
                            }
                        }
                    }
                }
            },
            onDismiss = onDismiss,
        )
    }
}
