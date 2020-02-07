package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.FragmentIndicatorsBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.hisp.dhis.android.core.program.ProgramIndicator

class IndicatorsFragment : FragmentGlobalAbstract(), IndicatorsView {

    @Inject
    lateinit var presenter: IndicatorsPresenter

    private lateinit var binding: FragmentIndicatorsBinding
    private lateinit var adapter: IndicatorsAdapter

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
        adapter = IndicatorsAdapter()
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

    override fun swapIndicators(indicators: List<Trio<ProgramIndicator, String, String>>) {
        if (adapter != null) {
            adapter.setIndicators(indicators)
        }

        binding.spinner.visibility = View.GONE

        if (!indicators.isNullOrEmpty()) {
            binding.emptyIndicators.visibility = View.GONE
        } else {
            binding.emptyIndicators.visibility = View.VISIBLE
        }
    }
}