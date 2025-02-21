package org.dhis2.commons.filters.periods.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.commons.R
import org.dhis2.commons.filters.periods.di.FilterPeriodsDialogComponentProvider
import org.dhis2.commons.filters.periods.di.FilterPeriodsDialogModule
import org.dhis2.commons.filters.periods.model.PeriodFilterType
import org.dhis2.commons.filters.periods.ui.viewmodel.FilterPeriodsDialogViewmodel
import org.dhis2.commons.filters.periods.ui.viewmodel.FilterPeriodsDialogViewmodelFactory
import javax.inject.Inject

class FilterPeriodsDialog : BottomSheetDialogFragment() {

    private var periodFilterTypeToSet: PeriodFilterType = PeriodFilterType.OTHER
    private var isDataSetFilter: Boolean = false

    companion object {
        const val FILTER_DIALOG = "FILTER_DIALOG"

        fun newPeriodsFilter(periodFilterType: PeriodFilterType, isDataSet: Boolean = false): FilterPeriodsDialog {
            return FilterPeriodsDialog().apply {
                periodFilterTypeToSet = periodFilterType
                isDataSetFilter = isDataSet
            }
        }
    }

    @Inject
    lateinit var factory: FilterPeriodsDialogViewmodelFactory.Factory

    val viewModel: FilterPeriodsDialogViewmodel by viewModels {
        factory.build()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as FilterPeriodsDialogComponentProvider).provideFilterPeriodsDialogComponent(
            FilterPeriodsDialogModule(),
        )?.inject(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
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
                FilterPeriodsDialogUI(
                    viewModel = viewModel,
                    periodFilterType = periodFilterTypeToSet,
                    isDataSetPeriodTypes = isDataSetFilter,
                    onDismiss = { dismiss() },
                )
            }
        }
    }
}
