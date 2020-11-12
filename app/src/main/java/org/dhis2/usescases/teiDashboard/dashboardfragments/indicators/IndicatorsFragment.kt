package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import dhis2.org.analytics.charts.data.Graph
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.databinding.FragmentIndicatorsBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity

class IndicatorsFragment : FragmentGlobalAbstract(), IndicatorsView {

    @Inject
    lateinit var presenter: IndicatorsPresenter

    private lateinit var binding: FragmentIndicatorsBinding
    private lateinit var adapter: AnalyticsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as TeiDashboardMobileActivity
        if (((context.applicationContext) as App).dashboardComponent() != null) {
            ((context.applicationContext) as App).dashboardComponent()!!
                .plus(
                    IndicatorsModule(
                        activity.programUid,
                        activity.teiUid, this
                    )
                )
                .inject(this)
        }
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
        adapter = AnalyticsAdapter(requireContext())
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
