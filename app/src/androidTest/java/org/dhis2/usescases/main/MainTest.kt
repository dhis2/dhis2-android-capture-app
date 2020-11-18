package org.dhis2.usescases.main

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.common.filters.filterRobot
import org.dhis2.usescases.BaseTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    override fun getPermissionsToBeAccepted(): Array<String> {
        return arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

  @Test
    fun checkHomeScreenRecyclerviewHasElements() {
        startActivity()
        homeRobot {
            checkViewIsNotEmpty()
        }
    }

    @Test
    fun shouldRedirectToLoginIfClickOnLogOut() {
        setupCredentials()
        startActivity()
        enableIntents()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnLogout()
            checkLogInIsLaunched()
        }
    }

    @Test
    fun shouldNavigateToHomeWhenBackPressed() {
        setupCredentials()
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
            pressBack()
            checkHomeIsDisplayed()
        }
    }

    @Test
    fun checkDateFilterSetInitialDateWhenOpenedAgain(){
        setupCredentials()
        startActivity()

        homeRobot {
            openFilters()
        }

        filterRobot {
            openDateFilter()
            clickOnFromToDateOption()
            selectDate(2020,6,15)
            acceptDateSelected()
            selectDate(2020,11,7)
            acceptDateSelected()
            clickOnFromToDateOption()
            checkDate(2020,6,15)
            acceptDateSelected()
            checkDate(2020,11,7)
        }
    }

    private fun startActivity() {
        rule.launchActivity(null)
    }
}
