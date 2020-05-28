package org.dhis2.usescases.settings

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.common.rules.DataBindingIdlingResourceRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.login.loginRobot
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.homeRobot
import org.junit.Ignore
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

    override fun getPermissionsToBeAccepted(): Array<String> {
        return arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    }

    @Test
    @Ignore
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
    @Ignore
    fun shouldRefillValuesWhenClickOnReservedValues() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        // on Manage Reserved 90% view should add ScrollTo
        // on Refill 90% view check custom view action
        settingsRobot {
            clickOnReservedValues()
            //    clickOnManageReservedValues()
            //    clickOnRefill(0)
            Thread.sleep(8000)
            // is not clicking
            // checkReservedValuesWasRefill(0)
            //   clickOnRefill(1)
            //   checkReservedValuesWasRefill(1)
            //    clickOnRefill(2)
            //    checkReservedValuesWasRefill(2)
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

    @Test
    @Ignore
    fun shouldSuccessfullyDeleteLocalData() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnDeleteLocalData()
            clickOnAcceptDelete()
            clickOnAcceptDialog()
            checkSnackBarIsShown()
        }
    }

    @Test
    @Ignore
    fun shouldRedirectToLoginWhenResetAppSucceed() {
        setupCredentials()
        enableIntents()
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnResetApp()
            Thread.sleep(1000)
            checkOnAcceptReset()
            clickOnAcceptDialog()
        }

        loginRobot {
            checkUsernameFieldIsClear()
            checkPasswordFieldIsClear()
        }
    }

    @Test
    @Ignore
    fun shouldShowGatewayNumberDisableWhenClickOnSMSSettings() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnSMSSettings()
            checkGatewayNumberFieldIsDisable()
            checkSMSSubmissionIsEnable()
        }
    }

    fun startActivity() {
        rule.launchActivity(null)
    }
}
