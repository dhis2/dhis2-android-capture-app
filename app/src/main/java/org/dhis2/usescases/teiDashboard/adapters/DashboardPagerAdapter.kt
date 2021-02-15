package org.dhis2.usescases.teiDashboard.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.dhis2.usescases.teiDashboard.MOBILE_DASHBOARD_PORTRAIT_SIZE
import org.dhis2.usescases.teiDashboard.createPortraitTabFragment

class DashboardPagerAdapter(
    fa: FragmentActivity,
    private val currentProgram: String?,
    private val teiUid: String,
    private val enrollmentUid: String?
) : FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        return createPortraitTabFragment(currentProgram,teiUid, enrollmentUid,position)
    }

    override fun getItemCount() = if (currentProgram != null) MOBILE_DASHBOARD_PORTRAIT_SIZE else 1
}
