package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import javax.inject.Inject
import org.dhis2.R
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.databinding.FragmentIndicatorsBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract

const val VISUALIZATION_TYPE = "VISUALIZATION_TYPE"

class IndicatorsFragment : FragmentGlobalAbstract(), IndicatorsView {

    @Inject
    lateinit var presenter: IndicatorsPresenter

    private lateinit var binding: FragmentIndicatorsBinding
    private val adapter: AnalyticsAdapter by lazy { AnalyticsAdapter(requireContext()) }
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
}
