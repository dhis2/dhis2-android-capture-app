package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import io.reactivex.functions.Consumer
import org.dhis2.R
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.FragmentIndicatorsBinding
import org.dhis2.usescases.general.DialogFragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsAdapter
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsContracts
import org.hisp.dhis.android.core.program.ProgramIndicator

class IndicatorsDialogFragment : DialogFragmentGlobalAbstract(),
    IndicatorsContracts.View {
    //@Inject
    private lateinit var presenter: IndicatorsContracts.Presenter
    private lateinit var binding: FragmentIndicatorsBinding
    private lateinit var adapter: IndicatorsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_indicators, container, false)
        adapter = IndicatorsAdapter()
        binding.indicatorsRecycler.setAdapter(adapter)
        return binding.getRoot()
    }

    override fun onResume() {
        super.onResume()
        binding.spinner.setVisibility(View.VISIBLE)
        //presenter.init(this);
    }

    override fun onPause() {
        //presenter.onDettach();
        super.onPause()
    }

    override fun swapIndicators(): Consumer<List<Trio<ProgramIndicator, String, String>>> {
        return Consumer { indicators: List<Trio<ProgramIndicator, String, String>>? ->
            if (adapter != null) {
                adapter!!.setIndicators(indicators)
            }
            binding.spinner.setVisibility(View.GONE)
            if (indicators != null && !indicators.isEmpty()) {
                binding.emptyIndicators.setVisibility(View.GONE)
            } else {
                binding.emptyIndicators.setVisibility(View.VISIBLE)
            }
        }
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, TAG)
    }

    companion object {
        private val TAG = IndicatorsDialogFragment::class.java.simpleName
        fun create(): IndicatorsDialogFragment {
            return IndicatorsDialogFragment()
        }
    }
}