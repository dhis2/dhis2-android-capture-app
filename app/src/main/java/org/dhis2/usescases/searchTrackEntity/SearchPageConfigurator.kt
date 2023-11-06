package org.dhis2.usescases.searchTrackEntity

import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.common.FeatureType

class SearchPageConfigurator(
    val searchRepository: SearchRepository,
) : NavigationPageConfigurator {

    private var canDisplayMap: Boolean = false
    private var canDisplayAnalytics: Boolean = false

    fun initVariables(): SearchPageConfigurator {
        canDisplayMap = programHasCoordinates()
        canDisplayAnalytics = programHasAnalytics()
        return this
    }

    override fun displayListView(): Boolean {
        return true
    }

    override fun displayTableView(): Boolean {
        return false
    }

    override fun displayMapView(): Boolean {
        return canDisplayMap
    }

    override fun displayAnalytics(): Boolean {
        return canDisplayAnalytics
    }

    internal fun programHasCoordinates(): Boolean {
        val program = searchRepository.currentProgram()?.let { programId ->
            searchRepository.getProgram(programId)
        } ?: return false

        val programHasCoordinates = program.featureType() != null && program.featureType() != FeatureType.NONE

        val programStagesHaveCoordinates by lazy {
            searchRepository.programStagesHaveCoordinates(program.uid())
        }

        val programAttributesHaveCoordinates by lazy {
            searchRepository.programAttributesHaveCoordinates(program.uid())
        }

        val teTypeHasCoordinates by lazy {
            searchRepository.trackedEntityType?.let { teType ->
                teType.featureType() != null && teType.featureType() != FeatureType.NONE
            } ?: false
        }

        val teAttributesHaveCoordinates by lazy {
            searchRepository.teTypeAttributesHaveCoordinates(program.trackedEntityType()?.uid())
        }

        val eventsHaveCoordinates by lazy {
            searchRepository.eventsHaveCoordinates(program.uid())
        }

        return programHasCoordinates ||
            programStagesHaveCoordinates ||
            programAttributesHaveCoordinates ||
            teTypeHasCoordinates ||
            teAttributesHaveCoordinates ||
            eventsHaveCoordinates
    }

    internal fun programHasAnalytics(): Boolean {
        val programUid = searchRepository.currentProgram() ?: return false

        return searchRepository.getProgramVisualizationGroups(programUid).isNotEmpty()
    }
}
