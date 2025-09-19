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
    private val getProgramSpecificSettings: GetProgramSpecificSettings = mock()
    private val viewmodel = SettingsProgramViewModel(getProgramSpecificSettings)

    @Test
    fun `Should initialize the settings program`() =
        runTest {
            val testSettings =
                listOf(
                    mock<SpecificSettings>(),
                )
            whenever(getProgramSpecificSettings()).doReturn(testSettings)

            viewmodel.programSettings.test {
                assertEquals(emptyList<SpecificSettings>(), expectMostRecentItem())
                assertEquals(testSettings, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }
}
