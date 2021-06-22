package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import org.dhis2.App
import org.dhis2.R
import org.dhis2.core.types.Tree
import org.dhis2.core.ui.tree.TreeAdapter
import org.dhis2.databinding.FragmentFeedbackContentBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.dashboardsfragments.enrollment.EnrollmentInfo
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class FeedbackContentFragment : FragmentGlobalAbstract(),
    FeedbackContentPresenter.FeedbackContentView {

    @Inject
    lateinit var presenter: FeedbackContentPresenter
    private lateinit var binding: FragmentFeedbackContentBinding
    private lateinit var activity: FeedbackActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity = context as FeedbackActivity

        if (((context.applicationContext) as App).userComponent() != null) {
            ((context.applicationContext) as App).userComponent()!!
                .plus(
                    FeedbackModule(
                        activity.programUid,
                        activity.teiUid,
                        activity.enrollmentUid,
                        context
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

        binding.shareFeedbackButton.setOnClickListener {
            presenter.shareFeedback(binding.failedCheckBox.isChecked)
        }

        adapter = TreeAdapter(listOf(FeedbackItemBinder(), FeedbackHelpItemBinder()),
            { node: Tree<*> ->
                presenter.expand(node)
            })

        binding.feedbackRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onResume() {
        val programType = arguments?.getSerializable(PROGRAM_TYPE) as ProgramType

        val feedbackMode = initFeedbackMode(programType)
        val criticalFilter: Boolean? = initCriticalQuestionFilter(programType)

        presenter.attach(
            this,
            activity.enrollmentUid,
            feedbackMode,
            criticalFilter,
            binding.failedCheckBox.isChecked
        )
        super.onResume()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    override fun render(state: FeedbackContentState) {
        return when (state) {
            is FeedbackContentState.Loading -> renderLoading()
            is FeedbackContentState.Loaded -> renderLoaded(
                state.feedback,
                state.validations
            )
            is FeedbackContentState.ValidationsWithError -> {
                renderError(getString(R.string.unexpected_error_message))
                showValidations(state.validations)
            }
            is FeedbackContentState.SharingFeedback -> shareFeedback(
                state.enrollmentInfo,
                state.serverUrl
            )
            is FeedbackContentState.NotFound -> renderError(getString(R.string.empty_tei_no_add))
            is FeedbackContentState.UnexpectedError -> renderError(getString(R.string.unexpected_error_message))
        }
    }

    private fun shareFeedback(
        enrollmentInfo: EnrollmentInfo,
        serverUrl: String
    ) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND

            val url = URL(serverUrl)
            val feedbackUrl =
                URL("https://feedback.psi-mis.org/${url.host}/${enrollmentInfo.enrollmentUid}")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val assessmentDateText =
                "${getString(R.string.feedback_share_conducted)}: ${dateFormat.format(enrollmentInfo.enrollmentDate)}"
            val assessmentTypeText =
                "${getString(R.string.feedback_share_assessment_type)}: ${enrollmentInfo.programName}"
            val urlText = "${getString(R.string.feedback_url)} \n $feedbackUrl"

            val finalText = "$assessmentDateText\n$assessmentTypeText\n\n$urlText"

            putExtra(Intent.EXTRA_TEXT, finalText)

            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
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

    private fun renderLoaded(feedback: Tree.Root<*>, validations: List<Validation>) {
        binding.msgFeedback.visibility = View.GONE
        binding.spinner.visibility = View.GONE
        binding.failedCheckBox.isEnabled = true

        setFeedbackAdapter(feedback)

        showValidations(validations)
    }

    private fun showValidations(validations: List<Validation>) {
        if (validations.isNotEmpty()) {
            val builder = SpannableStringBuilder()

            validations.forEach {
                when (it) {
                    is Validation.DataElementError -> {
                        val type = getString(R.string.feedback_error)
                        val resStringId = resources.getIdentifier(
                            it.message,
                            "string",
                            activity.packageName
                        )
                        val message = getString(resStringId, it.dataElement)

                        builder.appendln("$type: $message")
                    }
                    is Validation.DataElementWarning -> {
                        val type = getString(R.string.feedback_warning)
                        val resStringId = resources.getIdentifier(
                            it.message,
                            "string",
                            activity.packageName
                        )
                        val message = getString(resStringId, it.dataElement)

                        builder.appendln("$type: $message")
                    }
                    is Validation.ProgramStageWarning -> {
                        val type = getString(R.string.feedback_warning)
                        val resStringId = resources.getIdentifier(
                            it.message,
                            "string",
                            activity.packageName
                        )
                        val message = getString(resStringId, it.programStage)

                        builder.appendln("$type: $message")
                    }
                }
                //  builder.setSpan( ImageSpan(activity, R.drawable.ic_error), builder.length - 1, builder.length, 0)
            }

            val snackbar = Snackbar.make(requireView(), builder, Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(
                R.string.customactivityoncrash_error_activity_error_details_copy
            ) {
                val clipboard =
                    binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                val clip = ClipData.newPlainText("copy", builder)
                clipboard.setPrimaryClip(clip)
            }

            val snackbarView: View = snackbar.view
            val textView =
                snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            textView.maxLines = 25
            snackbar.show()
        }
    }

    private lateinit var adapter: TreeAdapter

    private fun setFeedbackAdapter(feedback: Tree.Root<*>) {
        adapter.refresh(feedback)
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
