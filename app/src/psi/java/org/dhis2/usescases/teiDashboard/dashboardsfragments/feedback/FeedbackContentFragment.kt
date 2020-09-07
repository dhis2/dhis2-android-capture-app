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
import org.dhis2.core.types.TreeNode
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
    private lateinit var feedbackMode: FeedbackMode

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

        val programType = arguments?.getSerializable(PROGRAM_TYPE) as ProgramType

        initFeedbackMode(programType)

        binding.feedbackRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        return binding.root
    }

    override fun onResume() {
        presenter.attach(this, feedbackMode)
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

    private fun initFeedbackMode(programType: ProgramType) {
        feedbackMode = if (programType == ProgramType.RDQA) {
            val rdqaFeedbackFilter = (arguments?.getSerializable(RDQA_FILTER) as RdqaFeedbackFilter)
            if (rdqaFeedbackFilter == RdqaFeedbackFilter.BY_INDICATOR) FeedbackMode.ByEvent() else FeedbackMode.ByTechnicalArea
        } else {
            val hnqisFeedbackFilter =
                (arguments?.getSerializable(HNQIS_FILTER) as HnqisFeedbackFilter)

            when (hnqisFeedbackFilter) {
                HnqisFeedbackFilter.CRITICAL -> FeedbackMode.ByEvent(true)
                HnqisFeedbackFilter.NON_CRITICAL -> FeedbackMode.ByEvent(false)
                HnqisFeedbackFilter.ALL -> FeedbackMode.ByEvent()
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

    private fun renderLoaded(nodes: List<TreeNode<*>>) {
        binding.msgFeedback.visibility = View.GONE
        binding.spinner.visibility = View.GONE
        binding.failedCheckBox.isEnabled = true

        setFeedbackAdapter(nodes)
    }

    private fun setFeedbackAdapter(nodes: List<TreeNode<*>>) {
        val adapter = TreeAdapter(nodes, listOf(FeedbackItemBinder(), FeedbackHelpItemBinder()))
        binding.feedbackRecyclerView.adapter = adapter
    }

    companion object {
        private const val PROGRAM_TYPE = "program_type"
        private const val RDQA_FILTER = "rdqa_filter"
        private const val HNQIS_FILTER = "hnqis_filter"

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
