package org.dhis2.usescases.teiDashboard.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.dhis2.usescases.teiDashboard.MOBILE_DASHBOARD_LANDSCAPE_SIZE
import org.dhis2.usescases.teiDashboard.createLandscapeTabFragment

class DashboardPagerTabletAdapter(
    fa: FragmentActivity,
    private val currentProgram: String?,
    private val teiUid: String,
    private val enrollmentUid: String?
) : FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        return createLandscapeTabFragment(currentProgram,teiUid,position)
    }

    override fun getItemCount() = if (currentProgram != null) {
        MOBILE_DASHBOARD_LANDSCAPE_SIZE
    } else {
        NO_FRAGMENT_DUE_TO_NO_PROGRAM_SELECTED
    }

    companion object {
        const val NO_FRAGMENT_DUE_TO_NO_PROGRAM_SELECTED = 0
    }
}
