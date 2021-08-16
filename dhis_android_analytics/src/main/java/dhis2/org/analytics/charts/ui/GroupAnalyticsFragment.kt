package dhis2.org.analytics.charts.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dhis2.org.R
import dhis2.org.analytics.charts.data.AnalyticGroup
import dhis2.org.analytics.charts.di.AnalyticsComponentProvider
import dhis2.org.analytics.charts.extensions.isNotCurrent
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentModule
import dhis2.org.databinding.AnalyticsGroupBinding
import dhis2.org.databinding.AnalyticsItemBinding
import javax.inject.Inject
import org.dhis2.commons.bindings.clipWithRoundedCorners
import org.dhis2.commons.dialogs.AlertBottomDialog
import org.hisp.dhis.android.core.common.RelativePeriod

const val ARG_MODE = "ARG_MODE"
const val ARG_UID = "ARG_UID"

class GroupAnalyticsFragment : Fragment() {

    private val mode: AnalyticMode by lazy {
        arguments?.getString(ARG_MODE)?.let { AnalyticMode.valueOf(it) } ?: AnalyticMode.HOME
    }
    private val uid: String? by lazy {
        arguments?.getString(ARG_UID)
    }
    private lateinit var binding: AnalyticsGroupBinding

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
                arguments = bundleArguments(AnalyticMode.PROGRAM, programUid)
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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        postponeEnterTransition()
        binding = DataBindingUtil.inflate(inflater, R.layout.analytics_group, container, false)
        ViewCompat.setTransitionName(binding.visualizationContainer, "contenttest")
        binding.lifecycleOwner = this
        binding.analyticsRecycler.adapter = adapter
        adapter.onRelativePeriodCallback =
            { chartModel: ChartModel, relativePeriod: RelativePeriod?, current: RelativePeriod? ->
                relativePeriod?.let {
                    if (it.isNotCurrent()) {
                        showAlertDialogCurrentPeriod(chartModel, relativePeriod, current)
                    } else {
                        groupViewModel.filterByPeriod(chartModel, mutableListOf(it))
                    }
                }
            }

        adapter.onOrgUnitCallback =
            { chartModel: ChartModel, orgUnitFilterType: OrgUnitFilterType ->
                if (orgUnitFilterType == OrgUnitFilterType.SELECTION) {
                    Log.d("GroupAnalyticsFrag", "onOrgUnitCallback")
                    groupViewModel.filterByOrgUnit()
                }
            }
        adapter.onResetFilterCallback = {
            groupViewModel.resetFilter()
        }
        binding.visualizationContainer.clipWithRoundedCorners()
        return binding.root
    }

    private fun showAlertDialogCurrentPeriod(
        chartModel: ChartModel,
        relativePeriod: RelativePeriod?,
        current: RelativePeriod?
    ) {
        val periodList = mutableListOf<RelativePeriod?>()
        AlertBottomDialog.instance
            .setTitle(getString(R.string.include_this_period_title))
            .setMessage(getString(R.string.include_this_period_body))
            .setNegativeButton(getString(R.string.no)) {
                periodList.add(relativePeriod)
                groupViewModel.filterByPeriod(chartModel, periodList)
            }
            .setPositiveButton(getString(R.string.yes)) {
                periodList.add(relativePeriod)
                periodList.add(current)
                groupViewModel.filterByPeriod(chartModel, periodList)
            }
            .show(parentFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupViewModel.chipItems.observe(
            viewLifecycleOwner,
            {
                if (it.isEmpty() || it.size < MIN_SIZE_TO_SHOW) {
                    binding.analyticChipGroup.visibility = View.GONE
                } else {
                    binding.analyticChipGroup.visibility = View.VISIBLE
                    disableToolbarElevation?.invoke()
                    addChips(it)
                }
                startPostponedEnterTransition()
            }
        )
        groupViewModel.analytics.observe(
            viewLifecycleOwner,
            { analytics ->
                adapter.submitList(analytics) {
                    binding.progress.visibility = View.GONE
                }
            }
        )
    }

    private fun addChips(list: List<AnalyticGroup>) {
        var idChip = 0
        list.forEachIndexed { index, analyticGroup ->
            binding.analyticChipGroup.addView(
                AnalyticsItemBinding.inflate(
                    layoutInflater,
                    binding.analyticChipGroup, false
                ).apply {
                    chip.id = idChip
                    chip.text = analyticGroup.name
                    chip.isChecked = index == 0
                    chip.tag = analyticGroup.uid
                    chip.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {
                            binding.progress.visibility = View.VISIBLE
                            groupViewModel.fetchAnalytics(buttonView.tag as String)
                        }
                    }
                }.root
            )
            idChip++
        }
    }

    fun sharedView() = binding.visualizationContainer
}
