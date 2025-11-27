package org.dhis2.commons.filters.periods.ui

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.parcelize.Parcelize
import org.dhis2.commons.R
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.periods.ui.viewmodel.FilterPeriodsDialogViewmodel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class FilterPeriodsDialog : BottomSheetDialogFragment() {
    companion object {
        const val FILTER_DIALOG = "FILTER_DIALOG"
        private const val TAG_FILTER_DIALOG_LAUNCH_MODE = "TAG_FILTER_DIALOG_LAUNCH_MODE"

        fun newPeriodsFilter(
            filterType: Filters,
            isFromToFilter: Boolean = false,
        ): FilterPeriodsDialog {
            val filterDialogLaunchMode =
                FilterDialogLaunchMode.NewPeriodDialog(
                    filterType = filterType,
                    isFromToFilter = isFromToFilter,
                )

            return FilterPeriodsDialog().apply {
                arguments =
                    bundleOf(
                        TAG_FILTER_DIALOG_LAUNCH_MODE to filterDialogLaunchMode,
                    )
            }
        }

        fun newDatasetsFilter(isFromToFilter: Boolean = false): FilterPeriodsDialog {
            val filterDialogLaunchMode =
                FilterDialogLaunchMode.NewDataSetPeriodDialog(
                    isFromToFilter = isFromToFilter,
                )

            return FilterPeriodsDialog().apply {
                arguments =
                    bundleOf(
                        TAG_FILTER_DIALOG_LAUNCH_MODE to filterDialogLaunchMode,
                    )
            }
        }
    }

    private lateinit var launchMode: FilterDialogLaunchMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        val arguments = arguments
        if (arguments != null) {
            launchMode = FilterDialogLaunchMode.fromBundle(arguments)
        }
    }

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
                val viewModel: FilterPeriodsDialogViewmodel =
                    koinViewModel<FilterPeriodsDialogViewmodel>(parameters = {
                        parametersOf(
                            context,
                            launchMode,
                        )
                    })
                FilterPeriodsDialogUI(
                    viewModel = viewModel,
                    launchMode = launchMode,
                    onDismiss = { dismiss() },
                )
            }
        }

    sealed interface FilterDialogLaunchMode : Parcelable {
        companion object {
            fun fromBundle(args: Bundle): FilterDialogLaunchMode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    args.getParcelable(TAG_FILTER_DIALOG_LAUNCH_MODE, FilterDialogLaunchMode::class.java)!!
                } else {
                    @Suppress("DEPRECATION")
                    args.getParcelable(TAG_FILTER_DIALOG_LAUNCH_MODE)!!
                }
        }

        val isFromToFilter: Boolean

        @Parcelize
        data class NewDataSetPeriodDialog(
            override val isFromToFilter: Boolean,
        ) : FilterDialogLaunchMode

        @Parcelize
        data class NewPeriodDialog(
            val filterType: Filters,
            override val isFromToFilter: Boolean,
        ) : FilterDialogLaunchMode
    }
}
