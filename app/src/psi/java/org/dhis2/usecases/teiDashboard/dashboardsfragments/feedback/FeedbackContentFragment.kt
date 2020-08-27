package org.dhis2.usecases.teiDashboard.dashboardsfragments.feedback

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.dhis2.R
import org.dhis2.databinding.FragmentFeedbackContentBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract

class FeedbackContentFragment : FragmentGlobalAbstract() {

/*    @Inject
    lateinit var presenter: IndicatorsPresenter*/

    private lateinit var binding: FragmentFeedbackContentBinding
    //private lateinit var adapter: IndicatorsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
/*        val activity = context as TeiDashboardMobileActivity
        if (((context.applicationContext) as App).dashboardComponent() != null) {
            ((context.applicationContext) as App).dashboardComponent()!!
                .plus(
                    IndicatorsModule(
                        activity.programUid,
                        activity.teiUid, this
                    )
                )
                .inject(this)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_feedback_content, container, false
        )

        val programType  = arguments?.getSerializable(PROGRAM_TYPE) as ProgramType

        if (programType == ProgramType.RDQA){
            binding.emptyFeedback.text = (arguments?.getSerializable(RDQA_FILTER) as RdqaFeedbackFilter).name
        } else {
            binding.emptyFeedback.text = (arguments?.getSerializable(HNQIS_FILTER) as HnqisFeedbackFilter).name
        }

        //adapter = IndicatorsAdapter()
        //binding.indicatorsRecycler.adapter = adapter
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        //binding.spinner.visibility = View.VISIBLE
        //presenter.init()
    }

    override fun onPause() {
        //presenter.onDettach()
        super.onPause()
    }

/*    override fun swapIndicators(indicators: List<Trio<ProgramIndicator, String, String>>) {
        if (adapter != null) {
            adapter.setIndicators(indicators)
        }

        binding.spinner.visibility = View.GONE

        if (!indicators.isNullOrEmpty()) {
            binding.emptyIndicators.visibility = View.GONE
        } else {
            binding.emptyIndicators.visibility = View.VISIBLE
        }
    }*/

    companion object {
        private const val PROGRAM_TYPE  = "program_type"
        private const val RDQA_FILTER  = "rdqa_filter"
        private const val HNQIS_FILTER  = "hnqis_filter"

        fun newInstanceByRDQA(
            RdqaFeedbackFilter: RdqaFeedbackFilter
        ): FeedbackContentFragment {
            val fragment = FeedbackContentFragment()

            val args = Bundle()
            args.putSerializable(PROGRAM_TYPE, ProgramType.RDQA)
            args.putSerializable(RDQA_FILTER, RdqaFeedbackFilter)
            fragment.arguments = args

            return fragment
        }

        fun newInstanceByHNQIS(
            HnqisFeedbackFilter: HnqisFeedbackFilter
        ): FeedbackContentFragment {
            val fragment = FeedbackContentFragment()

            val args = Bundle()
            args.putSerializable(PROGRAM_TYPE, ProgramType.HNQIS)
            args.putSerializable(HNQIS_FILTER, HnqisFeedbackFilter)
            fragment.arguments = args

            return fragment
        }
    }
}
