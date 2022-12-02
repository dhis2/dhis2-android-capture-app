package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import dhis2.org.analytics.charts.extensions.isNotCurrent
import dhis2.org.analytics.charts.ui.AnalyticsAdapter
import dhis2.org.analytics.charts.ui.AnalyticsModel
import dhis2.org.analytics.charts.ui.ChartModel
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import javax.inject.Inject
import org.dhis2.R
import org.dhis2.commons.dialogs.AlertBottomDialog
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OnOrgUnitSelectionFinished
import org.dhis2.databinding.FragmentIndicatorsBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

const val VISUALIZATION_TYPE = "VISUALIZATION_TYPE"

class IndicatorsFragment : FragmentGlobalAbstract(), IndicatorsView {

    @Inject
    lateinit var presenter: IndicatorsPresenter

    private lateinit var binding: FragmentIndicatorsBinding
    private val adapter: AnalyticsAdapter by lazy {
        AnalyticsAdapter().apply {
            onRelativePeriodCallback = { chartModel: ChartModel,
                relativePeriod: RelativePeriod?,
                current: RelativePeriod? ->
                relativePeriod?.let {
                    if (it.isNotCurrent()) {
                        showAlertDialogCurrentPeriod(chartModel, relativePeriod, current)
                    } else {
                        presenter.filterByPeriod(chartModel, mutableListOf(it))
                    }
                }
            }
            onOrgUnitCallback =
                { chartModel: ChartModel, orgUnitFilterType: OrgUnitFilterType ->
                    when (orgUnitFilterType) {
                        OrgUnitFilterType.SELECTION -> showOUTreeSelector(chartModel)
                        else -> presenter.filterByOrgUnit(
                            chartModel,
                            emptyList(),
                            orgUnitFilterType
                        )
                    }
                }
            onResetFilterCallback = { chartModel, filterType ->
                presenter.resetFilter(chartModel, filterType)
            }
        }
    }
    private val indicatorInjector by lazy { IndicatorInjector(this) }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        indicatorInjector.inject(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_indicators, container, false
        )
        binding.indicatorsRecycler.adapter = adapter
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.spinner.visibility = View.VISIBLE
        presenter.init()
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun swapAnalytics(analytics: List<AnalyticsModel>) {
        adapter.submitList(analytics)
        binding.spinner.visibility = View.GONE

        if (!analytics.isNullOrEmpty()) {
            binding.emptyIndicators.visibility = View.GONE
        } else {
            binding.emptyIndicators.visibility = View.VISIBLE
        }
    }

    private fun showAlertDialogCurrentPeriod(
        chartModel: ChartModel,
        relativePeriod: RelativePeriod?,
        current: RelativePeriod?
    ) {
        val periodList = mutableListOf<RelativePeriod>()
        AlertBottomDialog.instance
            .setTitle(getString(dhis2.org.R.string.include_this_period_title))
            .setMessage(getString(dhis2.org.R.string.include_this_period_body))
            .setNegativeButton(getString(dhis2.org.R.string.no)) {
                relativePeriod?.let { periodList.add(relativePeriod) }
                presenter.filterByPeriod(chartModel, periodList)
            }
            .setPositiveButton(getString(dhis2.org.R.string.yes)) {
                relativePeriod?.let { periodList.add(relativePeriod) }
                current?.let { periodList.add(current) }
                presenter.filterByPeriod(chartModel, periodList)
            }
            .show(parentFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    private fun showOUTreeSelector(chartModel: ChartModel) {
        val ouTreeFragment =
            OUTreeFragment.newInstance(
                true,
                chartModel.graph.orgUnitsSelected.toMutableList()
            )
        ouTreeFragment.selectionCallback = object : OnOrgUnitSelectionFinished {
            override fun onSelectionFinished(selectedOrgUnits: List<OrganisationUnit>) {
                presenter.filterByOrgUnit(
                    chartModel, selectedOrgUnits,
                    OrgUnitFilterType.SELECTION
                )
            }
        }
        ouTreeFragment.show(childFragmentManager, "OUTreeFragment")
    }
}
