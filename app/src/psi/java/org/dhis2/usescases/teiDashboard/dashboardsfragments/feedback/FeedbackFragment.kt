package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.FragmentFeedbackBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teiDashboard.adapters.FeedbackPagerAdapter
import javax.inject.Inject

class FeedbackFragment : FragmentGlobalAbstract(), FeedbackPresenter.FeedbackView {

    @Inject
    lateinit var presenter: FeedbackPresenter

    private lateinit var binding: FragmentFeedbackBinding
    private lateinit var adapter: FeedbackPagerAdapter

    private lateinit var activity: TeiDashboardMobileActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity = context as TeiDashboardMobileActivity

        if (((context.applicationContext) as App).dashboardComponent() != null) {
            ((context.applicationContext) as App).dashboardComponent()!!
                .plus(FeedbackModule(activity.programUid,activity.teiUid,activity.enrollmentUid))
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
            R.layout.fragment_feedback, container, false
        )

        val activity = context as TeiDashboardMobileActivity
        presenter.attach(this, activity.programUid)

        return binding.root
    }

    override fun onPause() {
        presenter.detach()
        super.onPause()
    }

    private fun setUpTabs(programType: ProgramType) {
        adapter = FeedbackPagerAdapter(this, programType)
        binding.feedbackPager.adapter = adapter
        TabLayoutMediator(
            binding.feedbackTabLayout,
            binding.feedbackPager
        ) { tab: TabLayout.Tab, position: Int ->

            if (programType == ProgramType.RDQA) {
                tab.text = rdqaTabTitles[position]
            } else {
                tab.text = hnqisTabTitles[position]
            }

        }.attach()
    }

    companion object {
        val rdqaTabTitles = listOf("By indicator", "By technical area")
        val hnqisTabTitles = listOf("All", "Critical", "Non Critical")
    }

    override fun render(state: FeedbackState) {
        return when (state) {
            is FeedbackState.Loading -> renderLoading()
            is FeedbackState.Loaded -> renderLoaded(state.feedbackProgram)
            is FeedbackState.ConfigurationError -> renderError(
                getString(R.string.program_type_configuration_error_message)
            )
            is FeedbackState.UnexpectedError -> renderError(getString(R.string.unexpected_error_message))
        }
    }

    private fun renderLoading() {
        binding.spinner.visibility = View.VISIBLE
        binding.errorFeedback.visibility = View.GONE
    }

    private fun renderError(text: String) {
        binding.spinner.visibility = View.GONE
        binding.errorFeedback.visibility = View.VISIBLE
        binding.errorFeedback.text = text
    }

    private fun renderLoaded(feedbackProgram: FeedbackProgram) {
        binding.errorFeedback.visibility = View.GONE
        binding.spinner.visibility = View.GONE
        setUpTabs(feedbackProgram.programType)
    }
}
