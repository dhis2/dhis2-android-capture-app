package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.databinding.ActivityFeedbackBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.teiDashboard.adapters.FeedbackPagerAdapter
import javax.inject.Inject

class FeedbackActivity : ActivityGlobalAbstract(), FeedbackPresenter.FeedbackView {

    @Inject
    lateinit var presenter: FeedbackPresenter

    private lateinit var binding: ActivityFeedbackBinding
    private lateinit var adapter: FeedbackPagerAdapter
    lateinit var teiUid: String
    lateinit var programUid: String
    lateinit var enrollmentUid: String

    override fun onCreate(savedInstanceState: Bundle?) {

        teiUid = intent.getStringExtra(Constants.TEI_UID) ?: ""
        programUid = intent.getStringExtra(Constants.PROGRAM_UID) ?: ""
        enrollmentUid = intent.getStringExtra(Constants.ENROLLMENT_UID) ?: ""

        if (((applicationContext) as App).userComponent() != null) {
            ((applicationContext) as App).userComponent()!!
                .plus(
                    FeedbackModule(
                        programUid,
                        teiUid,
                        enrollmentUid,
                        this
                    )
                )
                .inject(this)
        }

        setTheme(presenter.getProgramTheme(R.style.AppTheme))

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_feedback)

        presenter.attach(this, programUid)
    }

    override fun onPause() {
        presenter.detach()
        super.onPause()
    }

    override fun render(state: FeedbackState) {
        binding.back.setOnClickListener { back() }
        return when (state) {
            is FeedbackState.Loading -> renderLoading()
            is FeedbackState.Loaded -> renderLoaded(state.feedbackProgram)
            is FeedbackState.ConfigurationError -> renderTextError(
                getString(R.string.program_type_configuration_error_message)
            )
            is FeedbackState.UnexpectedError -> renderError(getString(R.string.unexpected_error_message))
        }
    }

    private fun renderLoading() {
        binding.spinner.visibility = View.VISIBLE
        binding.errorFeedback.visibility = View.GONE
    }

    private fun renderTextError(text: String) {
        binding.spinner.visibility = View.GONE
        binding.errorFeedback.visibility = View.VISIBLE
        binding.errorFeedback.text = text
    }

    private fun renderLoaded(feedbackProgram: FeedbackProgram) {
        binding.errorFeedback.visibility = View.GONE
        binding.spinner.visibility = View.GONE
        setUpTabs(feedbackProgram.programType)
    }

    private fun setUpTabs(programType: ProgramType) {
        adapter = FeedbackPagerAdapter(this, programType)
        binding.feedbackPager.adapter = adapter
        TabLayoutMediator(
            binding.feedbackTabLayout,
            binding.feedbackPager
        ) { tab: TabLayout.Tab, position: Int ->

            val tabKey = if (programType == ProgramType.RDQA) {
                rdqaTabTitles[position]
            } else {
                hnqisTabTitles[position]
            }

            tab.text = getString(resources.getIdentifier(tabKey, "string", packageName))

        }.attach()
    }

    companion object {
        val rdqaTabTitles =
            listOf("feedback_tab_rdqa_by_indicator", "feedback_tab_rdqa_by_technical_area")
        val hnqisTabTitles = listOf(
            "feedback_tab_hnqis_all",
            "feedback_tab_hnqis_critical",
            "feedback_tab_hnqis_non_critical"
        )

        fun intent(
            context: Context?,
            programUid: String?,
            enrollmentUid: String?,
            teiUid: String?
        ): Intent {
            val intent = Intent(
                context,
                FeedbackActivity::class.java
            )
            intent.putExtra(Constants.TEI_UID, teiUid)
            intent.putExtra(Constants.PROGRAM_UID, programUid)
            intent.putExtra(Constants.ENROLLMENT_UID, enrollmentUid)
            return intent
        }
    }
}
