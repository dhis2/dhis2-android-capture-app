package org.dhis2.usescases.teiDashboard.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback.FeedbackContentFragment
import org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback.HnqisFeedbackFilter
import org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback.ProgramType
import org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback.RdqaFeedbackMode

class FeedbackPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val programType: ProgramType
) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return if (programType == ProgramType.RDQA) {
            createFragmentByRDQA(position)
        } else {
            createFragmentByHNQIS(position)
        }
    }

    private fun createFragmentByRDQA(position: Int): Fragment {
        return when (position) {
            RDQA_BY_INDICATOR_POS -> FeedbackContentFragment.newInstanceByRDQA(RdqaFeedbackMode.BY_INDICATOR)
            RDQA_BY_TECHNICAL_ARE_POS -> FeedbackContentFragment.newInstanceByRDQA(
                RdqaFeedbackMode.BY_TECHNICAL_AREA
            )
            else -> throw IllegalStateException("Fragment not supported")
        }
    }

    private fun createFragmentByHNQIS(position: Int): Fragment {
        return when (position) {
            HNQIS_ALL_PO -> FeedbackContentFragment.newInstanceByHNQIS(HnqisFeedbackFilter.ALL)
            HNQIS_CRITICAL_PO -> FeedbackContentFragment.newInstanceByHNQIS(HnqisFeedbackFilter.CRITICAL)
            HNQIS_NON_CRITICAL_PO -> FeedbackContentFragment.newInstanceByHNQIS(HnqisFeedbackFilter.NON_CRITICAL)
            else -> throw IllegalStateException("Fragment not supported")
        }
    }

    override fun getItemCount() = if (programType == ProgramType.RDQA) RDQA_SIZE else HNQIS_SIZE

    companion object {
        const val RDQA_SIZE = 2
        const val HNQIS_SIZE = 3

        const val RDQA_BY_INDICATOR_POS = 0
        const val RDQA_BY_TECHNICAL_ARE_POS = 1

        const val HNQIS_ALL_PO = 0
        const val HNQIS_CRITICAL_PO = 1
        const val HNQIS_NON_CRITICAL_PO = 2
    }
}
