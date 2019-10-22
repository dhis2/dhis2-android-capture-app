package org.dhis2.utils

import me.toptas.fancyshowcase.FancyShowCaseView
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class HelpManagerTest {

    @Mock
    lateinit var step: FancyShowCaseView

    private val screenName = "Screen Name"

    @Before
    fun setUp() {
        HelpManager.getInstance().setScreenHelp(screenName, arrayListOf(step))
    }

    @Test
    fun testIsTutorialReadyForScreen() {
        assertTrue(HelpManager.getInstance().isTutorialReadyForScreen(screenName))
    }

    @Test
    fun testShowHelp() {
        HelpManager.getInstance().showHelp()
    }
}
