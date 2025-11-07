package org.dhis2.usescases.settingsprogram

import app.cash.turbine.test
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.settingsprogram.domain.GetProgramSpecificSettings
import org.dhis2.usescases.settingsprogram.model.SpecificSettings
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SettingsProgramPresenterTest {
    @Test
    fun `Should initialize the settings program`() =
        runTest {
            val getProgramSpecificSettings: GetProgramSpecificSettings = mock()
            val testSettings = listOf(mock<SpecificSettings>())
            whenever(getProgramSpecificSettings()).doReturn(testSettings)

            val viewmodel = SettingsProgramViewModel(getProgramSpecificSettings)

            viewmodel.programSettings.test {
                // Skip the initial empty emission if present
                val firstItem = awaitItem()
                if (firstItem.isEmpty()) {
                    // If first emission is empty, wait for the actual data
                    assertEquals(testSettings, awaitItem())
                } else {
                    // If first emission has data, verify it
                    assertEquals(testSettings, firstItem)
                }
                cancelAndConsumeRemainingEvents()
            }
        }
}
