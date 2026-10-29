package org.dhis2.usescases.about

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.dhis2.composetable.test.TestActivity
import org.dhis2.usescases.about.model.AboutInfo
import org.dhis2.usescases.about.model.AboutLink
import org.dhis2.usescases.about.ui.ABOUT_SDK_VALUE_TAG
import org.dhis2.usescases.about.ui.ABOUT_SERVER_VALUE_TAG
import org.dhis2.usescases.about.ui.ABOUT_USER_VALUE_TAG
import org.dhis2.usescases.about.ui.ABOUT_VERSION_VALUE_TAG
import org.dhis2.usescases.about.ui.AboutContent
import org.dhis2.usescases.about.ui.aboutLinkTag
import org.dhis2.usescases.about.ui.state.AboutUiState
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AboutScreenUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    private val aboutInfo =
        AboutInfo(
            version = "3.5.0",
            buildHash = "2c28d35",
            sdkVersion = "1.15.0",
            serverUrl = "play.im.dhis2.org/stable-2-43-1",
            username = "android",
        )

    private val clickedLinks = mutableListOf<AboutLink>()

    private fun setContent(info: AboutInfo? = aboutInfo) {
        composeTestRule.setContent {
            DHIS2Theme {
                AboutContent(
                    uiState = AboutUiState(info = info),
                    contentPadding = PaddingValues(),
                    onLinkClick = { clickedLinks.add(it) },
                )
            }
        }
    }

    @Test
    fun shouldShowInstallValuesWithTheBuildHashNextToTheVersion() {
        setContent()

        composeTestRule.onNodeWithTag(ABOUT_VERSION_VALUE_TAG).assertTextEquals("3.5.0 (2c28d35)")
        composeTestRule.onNodeWithTag(ABOUT_SDK_VALUE_TAG).assertTextEquals("1.15.0")
        composeTestRule
            .onNodeWithTag(ABOUT_SERVER_VALUE_TAG)
            .assertTextEquals("play.im.dhis2.org/stable-2-43-1")
        composeTestRule.onNodeWithTag(ABOUT_USER_VALUE_TAG).assertTextEquals("android")
    }

    @Test
    fun shouldShowOnlyTheVersionWhenThereIsNoBuildHash() {
        setContent(info = aboutInfo.copy(buildHash = null))

        composeTestRule.onNodeWithTag(ABOUT_VERSION_VALUE_TAG).assertTextEquals("3.5.0")
    }

    @Test
    fun shouldShowEmptyInstallValuesWhileTheyAreLoading() {
        setContent(info = null)

        composeTestRule.onNodeWithTag(ABOUT_VERSION_VALUE_TAG).assertTextEquals("")
        composeTestRule.onNodeWithTag(ABOUT_SERVER_VALUE_TAG).assertTextEquals("")
    }

    @Test
    fun shouldReportEveryLinkRowClick() {
        setContent()

        AboutLink.entries.forEach { link ->
            composeTestRule
                .onNodeWithTag(aboutLinkTag(link))
                .performScrollTo()
                .assertIsDisplayed()
                .performClick()
        }

        assertEquals(AboutLink.entries.toList(), clickedLinks)
    }
}
