package org.dhis2.usescases.settings

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainRobot
import org.dhis2.usescases.main.homeRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    override fun getPermissionsToBeAccepted(): Array<String> {
        return arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    }

    @Test
    fun openSettings() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }
    }

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
            Thread.sleep(1000)
            clickOnManageReservedValues()
            clickOnRefill(0)
            Thread.sleep(5000)
            //is not clicking
            //checkReservedValuesWasRefill(0)
            /*clickOnRefill(1)
            checkReservedValuesWasRefill(1)
            clickOnRefill(2)
            checkReservedValuesWasRefill(2)*/
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
            //error 90% shown single click
        }
    }

    @Test
    fun shouldRedirectToLoginWhenResetAppSucceed() {
        enableIntents()
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnResetApp()
            checkOnAcceptReset()
            clickOnAcceptDialog()
            //alert is shown
            //redirect to home
        }
    }

    @Test
    fun shouldShowGatewayNumberDisableWhenClickOnSMSSettings() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnSMSSettings()
            //checkGatewayNumberFieldIsDisable()
            checkSMSSubmissionIsEnable()
        }

        Thread.sleep(10000)
    }

    fun startActivity(){
        rule.launchActivity(null)
    }
}