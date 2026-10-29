package org.dhis2.usescases.about

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.BuildConfig
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainScreenType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val RELEASE_BUILD_TYPE = "release"

@RunWith(AndroidJUnit4::class)
class AboutTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun shouldCheckVersionsWhenOpenAboutScreen() {
        startActivity()

        aboutRobot(composeTestRule) {
            checkVersionNames(getAppVersionName(), getSDKVersionName())
        }
    }

    @Test
    fun shouldDisplayEveryLinkWhenOpenAboutScreen() {
        startActivity()

        aboutRobot(composeTestRule) {
            checkLinksAreDisplayed()
        }
    }

    private fun startActivity() {
        rule.launchActivity(
            MainActivity.intent(
                ApplicationProvider.getApplicationContext(),
                MainScreenType.About,
            )
        )
    }

    private fun getAppVersionName(): String =
        if (BuildConfig.BUILD_TYPE == RELEASE_BUILD_TYPE) {
            BuildConfig.VERSION_NAME
        } else {
            "${BuildConfig.VERSION_NAME} (${BuildConfig.GIT_SHA})"
        }

    private fun getSDKVersionName() = BuildConfig.SDK_VERSION
}
