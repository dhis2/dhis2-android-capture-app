package org.dhis2.data.forms.dataentry

import org.dhis2.form.ui.SectionHandler
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionHandlerTest {
    private val sectionHandler = SectionHandler()

    @Test
    fun `Should return visible position if it is section`() {
        val result = sectionHandler.getSectionPositionFromVisiblePosition(
            12,
            true,
            mockedSectionPositions(),
        )

        assertTrue(result == 12)
    }

    @Test
    fun `Should return correct position`() {
        arrayListOf(1, 4, 45, 100).forEachIndexed { index, visiblePosition ->
            val result = sectionHandler.getSectionPositionFromVisiblePosition(
                visiblePosition,
                false,
                mockedSectionPositions(),
            )
            assertTrue(result == mockedSectionPositions()[index])
        }
    }

    @Test
    fun `Should return no position`() {
        val result = sectionHandler.getSectionPositionFromVisiblePosition(
            5,
            false,
            arrayListOf(),
        )
        assertTrue(result == -1)
    }

    private fun mockedSectionPositions() = arrayListOf(0, 2, 5, 63)
}
