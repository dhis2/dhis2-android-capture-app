package org.dhis2.usescases.teiDashboard.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

import org.dhis2.usescases.notes.NotesFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment
import java.lang.IllegalStateException


class DashboardPagerTabletAdapter(
    fa: FragmentActivity,
    private val currentProgram: String?,
    private val teiUid: String,
    private val enrollmentUid: String?
) : FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> IndicatorsFragment()
            1 -> RelationshipFragment()
            2 -> NotesFragment.newTrackerInstance(currentProgram!!, teiUid)
            else -> throw IllegalStateException("Fragment not supported")
        }
    }

    override fun getItemCount() = if (currentProgram != null) {
        MOBILE_DASHBOARD_SIZE
    } else {
        NO_FRAGMENT_DUE_TO_NO_PROGRAM_SELECTED
    }


    companion object {
        const val MOBILE_DASHBOARD_SIZE = 3
        const val NO_FRAGMENT_DUE_TO_NO_PROGRAM_SELECTED = 0
    }
}
