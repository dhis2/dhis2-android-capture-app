package org.dhis2.usescases.teiDashboard.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.dhis2.R
import org.dhis2.usescases.notes.NotesFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.VISUALIZATION_TYPE
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.VisualizationType
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment
import org.dhis2.utils.isLandscape

class DashboardPagerAdapter(
    private val fragmentActivity: FragmentActivity,
    private val currentProgram: String?,
    private val teiUid: String,
    private val enrollmentUid: String?,
    private val displayAnalyticScreen: Boolean = true,
    private val displayRelationshipScreen: Boolean,
) : FragmentStateAdapter(fragmentActivity) {

    enum class DashboardPageType {
        TEI_DETAIL, ANALYTICS, RELATIONSHIPS, NOTES
    }

    private var indicatorsFragment: IndicatorsFragment? = null
    private var relationshipFragment: RelationshipFragment? = null
    private val landscapePages: List<DashboardPageType>
    private val portraitPages: List<DashboardPageType>

    init {
        landscapePages = mutableListOf<DashboardPageType>().apply {
            if (displayAnalyticScreen) add(DashboardPageType.ANALYTICS)
            if (displayRelationshipScreen) add(DashboardPageType.RELATIONSHIPS)
            if (currentProgram != null) add(DashboardPageType.NOTES)
        }
        portraitPages = mutableListOf<DashboardPageType>().apply {
            add(DashboardPageType.TEI_DETAIL)
            if (displayAnalyticScreen) add(DashboardPageType.ANALYTICS)
            if (displayRelationshipScreen) add(DashboardPageType.RELATIONSHIPS)
            if (currentProgram != null) add(DashboardPageType.NOTES)
        }
    }

    override fun createFragment(position: Int): Fragment {
        return createFragmentForPage(
            if (fragmentActivity.isLandscape()) {
                landscapePages[position]
            } else {
                portraitPages[position]
            },
        )
    }

    private fun createFragmentForPage(pageType: DashboardPageType): Fragment {
        return when (pageType) {
            DashboardPageType.TEI_DETAIL -> TEIDataFragment.newInstance(
                currentProgram,
                teiUid,
                enrollmentUid,
            )
            DashboardPageType.ANALYTICS -> {
                if (indicatorsFragment == null) {
                    indicatorsFragment = IndicatorsFragment().apply {
                        arguments = Bundle().apply {
                            putString(VISUALIZATION_TYPE, VisualizationType.TRACKER.name)
                        }
                    }
                }
                indicatorsFragment!!
            }
            DashboardPageType.RELATIONSHIPS -> {
                if (relationshipFragment == null) {
                    relationshipFragment = RelationshipFragment().apply {
                        arguments = RelationshipFragment.withArguments(
                            currentProgram,
                            teiUid,
                            enrollmentUid,
                            null,
                        )
                    }
                }
                relationshipFragment!!
            }
            DashboardPageType.NOTES -> NotesFragment.newTrackerInstance(currentProgram!!, teiUid)
        }
    }

    override fun getItemCount() =
        if (fragmentActivity.isLandscape()) landscapePages.size else portraitPages.size

    fun getNavigationPagePosition(navigationId: Int): Int {
        val pageType = when (navigationId) {
            R.id.navigation_details -> DashboardPageType.TEI_DETAIL
            R.id.navigation_analytics -> DashboardPageType.ANALYTICS
            R.id.navigation_relationships -> DashboardPageType.RELATIONSHIPS
            R.id.navigation_notes -> DashboardPageType.NOTES
            R.id.navigation_events -> null
            else -> null
        }

        return pageType?.let {
            if (fragmentActivity.isLandscape()) {
                landscapePages.indexOf(pageType)
            } else {
                portraitPages.indexOf(pageType)
            }
        } ?: NO_POSITION
    }

    fun pageType(position: Int): DashboardPageType {
        return if (fragmentActivity.isLandscape()) {
            landscapePages[position]
        } else {
            portraitPages[position]
        }
    }

    companion object {
        const val NO_POSITION = -1
    }
}
