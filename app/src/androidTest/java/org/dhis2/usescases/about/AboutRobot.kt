package org.dhis2.usescases.about

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.about.model.AboutLink
import org.dhis2.usescases.about.ui.ABOUT_SDK_VALUE_TAG
import org.dhis2.usescases.about.ui.ABOUT_VERSION_VALUE_TAG
import org.dhis2.usescases.about.ui.aboutLinkTag

fun aboutRobot(
    composeTestRule: ComposeTestRule,
    aboutBody: AboutRobot.() -> Unit,
) {
    AboutRobot(composeTestRule).apply {
        aboutBody()
    }
}

class AboutRobot(
    private val composeTestRule: ComposeTestRule,
) : BaseRobot() {
    @OptIn(ExperimentalTestApi::class)
    fun checkVersionNames(
        appVersion: String,
        sdkVersion: String,
    ) {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(ABOUT_VERSION_VALUE_TAG) and hasText(appVersion),
            TIMEOUT,
        )
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(ABOUT_SDK_VALUE_TAG) and hasText(sdkVersion),
            TIMEOUT,
        )
    }

    fun checkLinksAreDisplayed() {
        AboutLink.entries.forEach { link ->
            composeTestRule
                .onNodeWithTag(aboutLinkTag(link))
                .performScrollTo()
                .assertIsDisplayed()
        }
    }
}
