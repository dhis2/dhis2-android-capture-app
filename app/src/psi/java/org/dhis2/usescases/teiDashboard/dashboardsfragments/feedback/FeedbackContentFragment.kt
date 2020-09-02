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
import org.dhis2.core.ui.tree.TreeNode
import org.dhis2.databinding.FragmentFeedbackContentBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import javax.inject.Inject

class FeedbackContentFragment : FragmentGlobalAbstract(),
    FeedbackContentPresenter.FeedbackContentView {

    @Inject
    lateinit var presenter: FeedbackContentPresenter
    private lateinit var binding: FragmentFeedbackContentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (((context.applicationContext) as App).dashboardComponent() != null) {
            ((context.applicationContext) as App).dashboardComponent()!!
                .plus(FeedbackModule())
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

        return binding.root
    }

    override fun onResume() {
        val activity = context as TeiDashboardMobileActivity
        presenter.attach(this, activity.programUid)
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
            is FeedbackContentState.UnexpectedError -> renderError(getString(R.string.unexpected_error_message))
        }
    }

    private fun renderLoading() {
        binding.spinner.visibility = View.VISIBLE
        binding.msgFeedback.visibility = View.GONE
    }

    private fun renderError(text: String) {
        binding.spinner.visibility = View.GONE
        binding.msgFeedback.visibility = View.VISIBLE
        binding.msgFeedback.text = text
    }

    private fun renderLoaded(nodes: List<TreeNode<*>>) {
        binding.msgFeedback.visibility = View.GONE
        binding.spinner.visibility = View.GONE

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
