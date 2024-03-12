package dhis2.org.analytics.charts.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.utils.Utils
import dhis2.org.R
import dhis2.org.analytics.charts.data.AnalyticGroup
import dhis2.org.analytics.charts.di.AnalyticsComponentProvider
import dhis2.org.analytics.charts.extensions.isNotCurrent
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentModule
import dhis2.org.analytics.charts.ui.dialog.SearchColumnDialog
import dhis2.org.databinding.AnalyticsGroupBinding
import dhis2.org.databinding.AnalyticsItemBinding
import org.dhis2.commons.bindings.clipWithRoundedCorners
import org.dhis2.commons.bindings.scrollToPosition
import org.dhis2.commons.dialogs.AlertBottomDialog
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.hisp.dhis.android.core.common.RelativePeriod
import javax.inject.Inject

const val ARG_MODE = "ARG_MODE"
const val ARG_UID = "ARG_UID"

class GroupAnalyticsFragment : Fragment() {

    private val mode: AnalyticMode by lazy {
        arguments?.getString(ARG_MODE)?.let { AnalyticMode.valueOf(it) } ?: AnalyticMode.HOME
    }
    private val uid: String? by lazy {
        arguments?.getString(ARG_UID)
    }
    private var binding: AnalyticsGroupBinding? = null

    @Inject
    lateinit var analyticsViewModelFactory: GroupAnalyticsViewModelFactory
    private val groupViewModel: GroupAnalyticsViewModel by viewModels { analyticsViewModelFactory }
    private val adapter: AnalyticsAdapter by lazy { AnalyticsAdapter() }
    private var disableToolbarElevation: (() -> Unit)? = null

    companion object {
        fun forEnrollment(enrollmentUid: String): GroupAnalyticsFragment {
            return GroupAnalyticsFragment().apply {
                arguments = bundleArguments(AnalyticMode.ENROLLMENT, enrollmentUid)
            }
        }

        fun forProgram(programUid: String): GroupAnalyticsFragment {
            return GroupAnalyticsFragment().apply {
                arguments = bundleArguments(AnalyticMode.TRACKER_PROGRAM, programUid)
            }
        }

        fun forTrackerProgram(programUid: String): GroupAnalyticsFragment {
            return GroupAnalyticsFragment().apply {
                arguments = bundleArguments(AnalyticMode.EVENT_PROGRAM, programUid)
            }
        }

        fun forHome(): GroupAnalyticsFragment {
            return GroupAnalyticsFragment().apply {
                arguments = bundleArguments(AnalyticMode.HOME)
            }
        }

        fun forDataSet(dataSetUid: String): GroupAnalyticsFragment {
            return GroupAnalyticsFragment().apply {
                arguments = bundleArguments(AnalyticMode.DATASET, dataSetUid)
            }
        }
    }

    private fun bundleArguments(analyticMode: AnalyticMode, uid: String? = null): Bundle {
        return Bundle().apply {
            putString(ARG_MODE, analyticMode.name)
            uid?.let { putString(ARG_UID, uid) }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as AnalyticsComponentProvider)
            .provideAnalyticsFragmentComponent(AnalyticsFragmentModule(mode, uid))
            ?.inject(this)
        Utils.init(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        postponeEnterTransition()

        return AnalyticsGroupBinding.inflate(inflater, container, false).apply {
            ViewCompat.setTransitionName(visualizationContainer, "contenttest")
            lifecycleOwner = viewLifecycleOwner
            setUpAdapter()
            analyticsRecycler.adapter = adapter
            visualizationContainer.clipWithRoundedCorners()
        }.also {
            binding = it
        }.root
    }

    private fun setUpAdapter() {
        adapter.apply {
            onRelativePeriodCallback =
                { chartModel, relativePeriod, current, lineListingColumnId ->
                    groupViewModel.trackAnalyticsPeriodFilter(mode)
                    relativePeriod?.let {
                        if (it.isNotCurrent()) {
                            showAlertDialogCurrentPeriod(
                                chartModel,
                                relativePeriod,
                                current,
                                lineListingColumnId,
                            )
                        } else {
                            groupViewModel.filterByPeriod(
                                chartModel,
                                mutableListOf(it),
                                lineListingColumnId,
                            )
                        }
                    }
                }

            onOrgUnitCallback =
                { chartModel, orgUnitFilterType, lineListingColumnId ->
                    groupViewModel.trackAnalyticsOrgUnitFilter(mode)
                    when (orgUnitFilterType) {
                        OrgUnitFilterType.SELECTION -> showOUTreeSelector(
                            chartModel,
                            lineListingColumnId,
                        )

                        else -> groupViewModel.filterByOrgUnit(
                            chartModel,
                            emptyList(),
                            orgUnitFilterType,
                            lineListingColumnId,
                        )
                    }
                }
            onResetFilterCallback = { chartModel, filterType ->
                groupViewModel.trackAnalyticsFilterReset(mode)
                groupViewModel.resetFilter(chartModel, filterType)
            }

            onChartTypeChanged = {
                groupViewModel.trackChartTypeChanged(mode)
            }

            onSearchCallback = { chartModel, column ->
                showValueFilter(chartModel, column)
            }
        }
    }

    private fun showAlertDialogCurrentPeriod(
        chartModel: ChartModel,
        relativePeriod: RelativePeriod?,
        current: RelativePeriod?,
        lineListingColumnId: Int?,
    ) {
        val periodList = mutableListOf<RelativePeriod>()
        AlertBottomDialog.instance
            .setTitle(getString(R.string.include_this_period_title))
            .setMessage(getString(R.string.include_this_period_body))
            .setNegativeButton(getString(R.string.no)) {
                relativePeriod?.let { periodList.add(relativePeriod) }
                groupViewModel.filterByPeriod(chartModel, periodList, lineListingColumnId)
            }
            .setPositiveButton(getString(R.string.yes)) {
                relativePeriod?.let { periodList.add(relativePeriod) }
                current?.let { periodList.add(current) }
                groupViewModel.filterByPeriod(chartModel, periodList, lineListingColumnId)
            }
            .show(parentFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    private fun showOUTreeSelector(chartModel: ChartModel, lineListingColumnId: Int?) {
        OUTreeFragment.Builder()
            .showAsDialog()
            .withPreselectedOrgUnits(
                chartModel.graph.orgUnitsSelected(lineListingColumnId).toMutableList(),
            )
            .onSelection { selectedOrgUnits ->
                groupViewModel.filterByOrgUnit(
                    chartModel,
                    selectedOrgUnits,
                    OrgUnitFilterType.SELECTION,
                    lineListingColumnId,
                )
            }
            .build()
            .show(childFragmentManager, "OUTreeFragment")
    }

    private fun showValueFilter(chartModel: ChartModel, column: Int) {
        SearchColumnDialog(
            chartModel.graph.categories[column],
            onSearch = {
                groupViewModel.filterLineListingRows(chartModel, column, it)
            },
        ).show(childFragmentManager, SearchColumnDialog.TAG)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupViewModel.chipItems.observe(viewLifecycleOwner) { chipResult ->
            when {
                chipResult.isSuccess -> {
                    val chips = chipResult.getOrDefault(emptyList())
                    if (chips.isEmpty() || chips.size < MIN_SIZE_TO_SHOW) {
                        binding?.analyticChipGroup?.visibility = View.GONE
                    } else {
                        binding?.analyticChipGroup?.visibility = View.VISIBLE
                        disableToolbarElevation?.invoke()
                        addChips(chips)
                    }
                }

                chipResult.isFailure -> {
                    binding?.progressLayout?.visibility = View.GONE
                    binding?.emptyAnalytics?.apply {
                        visibility = View.VISIBLE
                        text = getString(R.string.visualization_groups_failure)
                    }
                }
            }
            startPostponedEnterTransition()
        }
        groupViewModel.analytics.observe(viewLifecycleOwner) { analytics ->
            when {
                analytics.isSuccess -> adapter.submitList(analytics.getOrDefault(emptyList())) {
                    binding?.progressLayout?.visibility = View.GONE
                }

                analytics.isFailure -> {
                    binding?.progressLayout?.visibility = View.GONE
                    binding?.emptyAnalytics?.apply {
                        visibility = View.VISIBLE
                        text = getString(R.string.visualization_failure)
                    }
                }
            }
        }
    }

    private fun addChips(list: List<AnalyticGroup>) {
        var idChip = 0
        list.forEachIndexed { index, analyticGroup ->
            binding?.analyticChipGroup?.addView(
                AnalyticsItemBinding.inflate(
                    layoutInflater,
                    binding?.analyticChipGroup,
                    false,
                ).apply {
                    chip.id = idChip
                    chip.text = analyticGroup.name
                    chip.isChecked = index == 0
                    chip.tag = analyticGroup.uid
                    chip.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {
                            binding?.analyticChipGroupContainer
                                ?.scrollToPosition(chip.tag as String)
                            binding?.progressLayout?.visibility = View.VISIBLE
                            groupViewModel.fetchAnalytics(buttonView.tag as String)
                        }
                    }
                }.root,
            )
            idChip++
        }
    }

    fun sharedView() = binding?.visualizationContainer
}
