package org.dhis2.usecases.teiDashboard.dashboardsfragments.feedback

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.dhis2.R
import org.dhis2.databinding.FragmentFeedbackBinding
import org.dhis2.usecases.teiDashboard.adapters.FeedbackPagerAdapter
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity

class FeedbackFragment : FragmentGlobalAbstract() {

    /*@Inject
    lateinit var presenter: IndicatorsPresenter*/

    private lateinit var binding: FragmentFeedbackBinding
    private lateinit var adapter: FeedbackPagerAdapter

    /* private lateinit var adapter: IndicatorsAdapter
*/
    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as TeiDashboardMobileActivity
        /*if (((context.applicationContext) as App).dashboardComponent() != null) {
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
            R.layout.fragment_feedback, container, false
        )

        initializeTabAndPager()

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

    private fun initializeTabAndPager() {
        val programType = ProgramType.HNQIS

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
        val rdqaTabTitles = listOf("By indicator", "By technical area")
        val hnqisTabTitles = listOf("All", "Critical", "Non Critical")
    }
}
