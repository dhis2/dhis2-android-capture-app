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

    //To check
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
