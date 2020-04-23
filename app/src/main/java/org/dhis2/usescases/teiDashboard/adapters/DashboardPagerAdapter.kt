package org.dhis2.usescases.teiDashboard.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.lang.IllegalStateException
import org.dhis2.usescases.notes.NotesFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.TEIDataFragment

class DashboardPagerAdapter(
    fa: FragmentActivity,
    private val currentProgram: String?,
    private val teiUid: String,
    private val enrollmentUid: String?
) : FragmentStateAdapter(fa) {

    private var indicatorsFragment: IndicatorsFragment? = null
    private var relationshipFragment: RelationshipFragment? = null

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TEIDataFragment.newInstance(currentProgram, teiUid, enrollmentUid)
            1 -> {
                if (indicatorsFragment == null) {
                    indicatorsFragment = IndicatorsFragment()
                }
                indicatorsFragment!!
            }
            2 -> {
                if (relationshipFragment == null) {
                    relationshipFragment = RelationshipFragment()
                }
                relationshipFragment!!
            }
            3 -> NotesFragment.newTrackerInstance(currentProgram!!, teiUid)
            else -> throw IllegalStateException("Fragment not supported")
        }
    }

    override fun getItemCount() = if (currentProgram != null) MOBILE_DASHBOARD_SIZE else 1

    companion object {
        const val MOBILE_DASHBOARD_SIZE = 4
    }
}
