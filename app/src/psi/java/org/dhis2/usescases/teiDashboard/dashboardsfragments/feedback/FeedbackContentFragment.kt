package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.FragmentFeedbackContentBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewAdapter
import tellh.com.recyclertreeview_lib.TreeViewAdapter.OnTreeNodeListener
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

/*        val programType  = arguments?.getSerializable(PROGRAM_TYPE) as ProgramType

        if (programType == ProgramType.RDQA){
            binding.emptyFeedback.text = (arguments?.getSerializable(RDQA_FILTER) as RdqaFeedbackFilter).name
        } else {
            binding.emptyFeedback.text = (arguments?.getSerializable(HNQIS_FILTER) as HnqisFeedbackFilter).name
        }*/

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

    override fun onPause() {
        presenter.detach()
        super.onPause()
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
        val adapter =
            TreeViewAdapter(nodes, listOf(FeedbackItemNodeBinder(), FeedbackHelpItemNodeBinder()))
        binding.feedbackRecyclerView.adapter = adapter
        binding.feedbackRecyclerView.itemAnimator = null

        adapter.setOnTreeNodeListener(object : OnTreeNodeListener {
            override fun onClick(node: TreeNode<*>, holder: RecyclerView.ViewHolder): Boolean {
                if (!node.isLeaf) {
                    //Update and toggle the node.
                    onToggle(!node.isExpand, holder)
                }
                return false
            }

            override fun onToggle(isExpand: Boolean, holder: RecyclerView.ViewHolder) {
                val dirViewHolder: FeedbackItemNodeBinder.ViewHolder =
                    holder as FeedbackItemNodeBinder.ViewHolder
                val arrow: ImageView = dirViewHolder.arrow
                val rotateDegree = if (isExpand) 180 else -180
                arrow.animate().rotationBy(rotateDegree.toFloat())
                    .start()
            }
        })
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
