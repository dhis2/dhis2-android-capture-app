package org.dhis2.usescases.searchTrackEntity

import com.google.common.truth.Truth.assertThat
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class SearchPageConfiguratorTest {

    private val searchRepository: SearchRepository = mock()
    private val program: Program = mock()
    private val teType: TrackedEntityType = mock()
    private val visualizationGroup: AnalyticsDhisVisualizationsGroup = mock()

    private val programUid: String = "programUid"
    private val teTypeUid: String = "teTypeUid"

    private lateinit var configurator: SearchPageConfigurator

    @Before
    fun setup() {
        whenever(searchRepository.currentProgram()).doReturn(programUid)
        whenever(searchRepository.getProgram(programUid)).doReturn(program)
        whenever(program.featureType()).doReturn(FeatureType.POINT)
        whenever(program.uid()).doReturn(programUid)
        whenever(program.trackedEntityType()).doReturn(teType)
        whenever(teType.uid()).doReturn(teTypeUid)

        configurator = SearchPageConfigurator(searchRepository)
    }

    @Test
    fun display_map_shortcut_to_false_if_no_program() {
        whenever(searchRepository.currentProgram()).doReturn(null)

        val hasCoordinates = configurator.programHasCoordinates()

        assertThat(hasCoordinates).isFalse()

        verify(searchRepository).currentProgram()
        verifyNoMoreInteractions(searchRepository)
    }

    @Test
    fun display_map_shortcut_to_true_if_program_has_coordinates() {
        val hasCoordinates = configurator.programHasCoordinates()

        assertThat(hasCoordinates).isTrue()

        verify(searchRepository).currentProgram()
        verify(searchRepository).getProgram(programUid)
        verifyNoMoreInteractions(searchRepository)
    }

    @Test
    fun display_map_do_not_shortcut_if_no_coordinates() {
        whenever(program.featureType()).doReturn(FeatureType.NONE)

        val hasCoordinates = configurator.programHasCoordinates()

        assertThat(hasCoordinates).isFalse()

        verify(searchRepository).currentProgram()
        verify(searchRepository).getProgram(programUid)
        verify(searchRepository).programStagesHaveCoordinates(programUid)
        verify(searchRepository).programAttributesHaveCoordinates(programUid)
        verify(searchRepository).trackedEntityType
        verify(searchRepository).teTypeAttributesHaveCoordinates(any())
        verify(searchRepository).eventsHaveCoordinates(programUid)
        verifyNoMoreInteractions(searchRepository)
    }

    @Test
    fun display_analytics_shortcut_to_false_if_no_program() {
        whenever(searchRepository.currentProgram()).doReturn(null)

        val hasAnalytics = configurator.programHasAnalytics()

        assertThat(hasAnalytics).isFalse()

        verify(searchRepository).currentProgram()
        verifyNoMoreInteractions(searchRepository)
    }
}
