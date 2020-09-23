package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import org.dhis2.App
import org.dhis2.R
import org.dhis2.core.ui.tree.TreeAdapter
import org.dhis2.core.types.Tree
import org.dhis2.databinding.FragmentFeedbackContentBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import javax.inject.Inject

class FeedbackContentFragment : FragmentGlobalAbstract(),
    FeedbackContentPresenter.FeedbackContentView {

    @Inject
    lateinit var presenter: FeedbackContentPresenter
    private lateinit var binding: FragmentFeedbackContentBinding
    private lateinit var activity: TeiDashboardMobileActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity = context as TeiDashboardMobileActivity

        if (((context.applicationContext) as App).dashboardComponent() != null) {
            ((context.applicationContext) as App).dashboardComponent()!!
                .plus(FeedbackModule(activity.programUid, activity.teiUid, activity.enrollmentUid))
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
            R.layout.fragment_feedback_content, container, false
        )

        binding.feedbackRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        binding.failedCheckBox.setOnClickListener {
            presenter.changeOnlyFailedFilter(binding.failedCheckBox.isChecked)
        }

        return binding.root
    }

    override fun onResume() {
        val programType = arguments?.getSerializable(PROGRAM_TYPE) as ProgramType

        val feedbackMode = initFeedbackMode(programType)
        val criticalFilter: Boolean? = initCriticalQuestionFilter(programType)

        presenter.attach(this, feedbackMode, criticalFilter, binding.failedCheckBox.isChecked)
        super.onResume()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    override fun render(state: FeedbackContentState) {
        return when (state) {
            is FeedbackContentState.Loading -> renderLoading()
            is FeedbackContentState.Loaded -> renderLoaded(state.feedback)
            is FeedbackContentState.NotFound -> renderError(getString(R.string.empty_tei_no_add))
            is FeedbackContentState.UnexpectedError -> renderError(getString(R.string.unexpected_error_message))
        }
    }

    private fun initFeedbackMode(programType: ProgramType): FeedbackMode {
        return if (programType == ProgramType.RDQA) {
            val rdqaFeedbackFilter = (arguments?.getSerializable(RDQA_MODE) as RdqaFeedbackMode)
            if (rdqaFeedbackFilter == RdqaFeedbackMode.BY_INDICATOR) FeedbackMode.ByEvent else FeedbackMode.ByTechnicalArea
        } else {
            FeedbackMode.ByEvent
        }
    }

    private fun initCriticalQuestionFilter(programType: ProgramType): Boolean? {
        return if (programType == ProgramType.RDQA) {
            null
        } else {
            val hnqisFeedbackFilter =
                (arguments?.getSerializable(HNQIS_FILTER) as HnqisFeedbackFilter)

            when (hnqisFeedbackFilter) {
                HnqisFeedbackFilter.CRITICAL -> true
                HnqisFeedbackFilter.NON_CRITICAL -> false
                HnqisFeedbackFilter.ALL -> null
            }
        }
    }

    private fun renderLoading() {
        binding.spinner.visibility = View.VISIBLE
        binding.msgFeedback.visibility = View.GONE
        binding.failedCheckBox.isEnabled = false
    }

    private fun renderError(text: String) {
        binding.spinner.visibility = View.GONE
        binding.msgFeedback.visibility = View.VISIBLE
        binding.msgFeedback.text = text
        binding.failedCheckBox.isEnabled = false
    }

    private fun renderLoaded(feedback: Tree.Root<*>) {
        binding.msgFeedback.visibility = View.GONE
        binding.spinner.visibility = View.GONE
        binding.failedCheckBox.isEnabled = true

        setFeedbackAdapter(feedback)
    }

    private fun setFeedbackAdapter(feedback: Tree.Root<*>) {
        val adapter = TreeAdapter(feedback, listOf(FeedbackItemBinder(), FeedbackHelpItemBinder()),
            {
                presenter.expand(it)
            })


        binding.feedbackRecyclerView.adapter = adapter
    }

    companion object {
        private const val PROGRAM_TYPE = "program_type"
        private const val RDQA_MODE = "rdqa_mode"
        private const val HNQIS_FILTER = "hnqis_filter"

        fun newInstanceByRDQA(
            RdqaFeedbackMode: RdqaFeedbackMode
        ): FeedbackContentFragment {
            val fragment = FeedbackContentFragment()

            val args = Bundle()
            args.putSerializable(PROGRAM_TYPE, ProgramType.RDQA)
            args.putSerializable(RDQA_MODE, RdqaFeedbackMode)
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
