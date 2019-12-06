package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.indicators

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import io.reactivex.functions.Consumer
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.DialogEventIndicatorsBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureModule
import org.dhis2.usescases.general.DialogFragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsAdapter
import org.hisp.dhis.android.core.program.ProgramIndicator
import javax.inject.Inject

class EventIndicatorsDialogFragment : DialogFragmentGlobalAbstract(),
    EventIndicatorsContracts.View {

    @Inject
    lateinit var presenter: EventIndicatorsContracts.Presenter

    private lateinit var binding: DialogEventIndicatorsBinding
    private lateinit var adapter: IndicatorsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as EventCaptureActivity
        if ((context.applicationContext as App).userComponent() != null) {
            (context.applicationContext as App)
                .userComponent()?.plus(EventCaptureModule(activity.eventUid,
                    activity.programUid))
                ?.plus(
                    EventIndicatorsModule(
                        activity.programUid,
                        activity.eventUid
                    )
                )?.inject(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_event_indicators, container, false)
        adapter = IndicatorsAdapter()
        binding.indicatorsRecycler.adapter = adapter
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.spinner.visibility = View.VISIBLE
        presenter.init(this)
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun onStart() {
        dialog!!.window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        super.onStart()
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, TAG)
    }

    override fun swapIndicators(): Consumer<List<Trio<ProgramIndicator, String, String>>> {
        return Consumer { indicators: List<Trio<ProgramIndicator, String, String>>? ->
            adapter.setIndicators(indicators)

            binding.spinner.visibility = View.GONE
            if (indicators != null && indicators.isNotEmpty()) {
                binding.emptyIndicators.visibility = View.GONE
            } else {
                binding.emptyIndicators.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private val TAG = EventIndicatorsDialogFragment::class.java.simpleName

        fun create(): EventIndicatorsDialogFragment {
            return EventIndicatorsDialogFragment()
        }
    }
}