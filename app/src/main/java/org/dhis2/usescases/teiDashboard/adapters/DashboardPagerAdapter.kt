package org.dhis2.usescases.teiDashboard.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.lang.IllegalStateException
import org.dhis2.R
import org.dhis2.usescases.notes.NotesFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment
import org.dhis2.utils.isLandscape

class DashboardPagerAdapter(
    fa: FragmentActivity,
    private val currentProgram: String?,
    private val teiUid: String,
    private val enrollmentUid: String?
) : FragmentStateAdapter(fa) {

    private var indicatorsFragment: IndicatorsFragment? = null
    private var relationshipFragment: RelationshipFragment? = null

    override fun createFragment(position: Int): Fragment {
        return if (isLandscape()) {
            createLandscapeFragment(position)
        } else {
            createPortraitFragment(position)
        }
    }

    private fun createLandscapeFragment(position: Int): Fragment {
        return when (position) {
            0 -> IndicatorsFragment()
            1 -> RelationshipFragment()
            2 -> NotesFragment.newTrackerInstance(currentProgram!!, teiUid)
            else -> throw IllegalStateException("Fragment not supported")
        }
    }

    private fun createPortraitFragment(position: Int): Fragment {
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

    override fun getItemCount() =
        if (isLandscape()) getLandscapeItemCount() else getPortraitItemCount()

    private fun getPortraitItemCount(): Int {
        return if (currentProgram != null) PORTRAIT_DASHBOARD_SIZE else 1
    }

    private fun getLandscapeItemCount(): Int {
        return if (currentProgram != null) {
            LANDSCAPE_DASHBOARD_SIZE
        } else {
            NO_FRAGMENT_DUE_TO_NO_PROGRAM_SELECTED
        }
    }

    fun getNavigationPagePosition(navigationId: Int): Int {
        return when (navigationId) {
            R.id.navigation_details -> if (isLandscape()) -1 else 0
            R.id.navigation_events -> -1
            R.id.navigation_analytics -> if (isLandscape()) 0 else 1
            R.id.navigation_relationships -> if (isLandscape()) 1 else 2
            R.id.navigation_notes -> if (isLandscape()) 2 else 3
            else -> -1
        }
    }

    companion object {
        const val PORTRAIT_DASHBOARD_SIZE = 4
        const val LANDSCAPE_DASHBOARD_SIZE = 3
        const val NO_FRAGMENT_DUE_TO_NO_PROGRAM_SELECTED = 0
    }
}
