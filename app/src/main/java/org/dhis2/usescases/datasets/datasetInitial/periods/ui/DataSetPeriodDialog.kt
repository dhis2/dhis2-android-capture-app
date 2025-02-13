package org.dhis2.usescases.datasets.datasetInitial.periods.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.bindings.app
import org.dhis2.commons.R
import org.dhis2.commons.date.toUiStringResource
import org.dhis2.commons.dialogs.bottomsheet.bottomSheetInsets
import org.dhis2.commons.dialogs.bottomsheet.bottomSheetLowerPadding
import org.dhis2.commons.periods.ui.PeriodSelectorContent
import org.dhis2.usescases.datasets.datasetInitial.periods.DatasetPeriodDialogModule
import org.dhis2.usescases.datasets.datasetInitial.periods.DatasetPeriodViewModel
import org.dhis2.usescases.datasets.datasetInitial.periods.DatasetPeriodViewModelFactory
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import java.util.Date
import javax.inject.Inject

class DataSetPeriodDialog(
    private val dataset: String,
    private val periodType: PeriodType,
    private val openFuturePeriods: Int,
) : BottomSheetDialogFragment() {

    lateinit var onDateSelectedListener: (Date) -> Unit

    @Inject
    lateinit var viewModelFactory: DatasetPeriodViewModelFactory

    val viewModel: DatasetPeriodViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        app().userComponent()?.plus(DatasetPeriodDialogModule())?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                DHIS2Theme {
                    val scrollState = rememberLazyListState()
                    BottomSheetShell(
                        uiState = BottomSheetShellUIState(
                            title = getString(periodType.toUiStringResource()),
                            showTopSectionDivider = true,
                            bottomPadding = bottomSheetLowerPadding(),
                        ),
                        onDismiss = { dismiss() },
                        windowInsets = { bottomSheetInsets() },
                        contentScrollState = scrollState,
                        content = {
                            val periods = viewModel.fetchPeriods(
                                dataset,
                                periodType,
                                openFuturePeriods,
                            ).collectAsLazyPagingItems()
                            PeriodSelectorContent(
                                periods = periods,
                                scrollState = scrollState,
                            ) { selectedDate ->
                                onDateSelectedListener(selectedDate)
                                dismiss()
                            }
                        },
                    )
                }
            }
        }
    }
}
