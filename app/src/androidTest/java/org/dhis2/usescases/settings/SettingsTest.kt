package org.dhis2.usescases.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.common.rules.DataBindingIdlingResourceRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.homeRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    @Rule
    @JvmField
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule(rule)

    @Test
    fun shouldFindEditPeriodDisabledWhenClickOnSyncData() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnSyncData()
            checkEditPeriodIsDisableForData()
        }
    }

    @Test
    fun shouldFindEditDisabledWhenClickOnSyncConfiguration() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnSyncConfiguration()
            checkEditPeriodIsDisableForConfiguration()
        }
    }

    @Test
    fun shouldFindEditDisableWhenClickOnSyncParameters() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnSyncParameters()
            checkEditPeriodIsDisableForParameters()
        }
    }

    @Test
    fun shouldRefillValuesWhenClickOnReservedValues() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnReservedValues()
            clickOnManageReservedValues()
        }
    }

    @Test
    fun shouldSuccessfullyOpenLogs() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnOpenSyncErrorLog()
            checkLogViewIsDisplayed()
        }
    }

    fun startActivity() {
        rule.launchActivity(null)
    }
}
