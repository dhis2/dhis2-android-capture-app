package org.dhis2.usescases.jira

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JiraTest: BaseTest() {
    private lateinit var mainRobot: MainRobot

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    override fun getPermissionsToBeAccepted(): Array<String> {
        return arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    }

    override fun setUp() {
        super.setUp()
        mainRobot = MainRobot()
    }

    @Test
    fun openOnJiraIssue() {
        startActivity()
        mainRobot.clickOnNavigationDrawerMenu()
                .clickJiraIssue()
    }

    fun startActivity(){
        rule.launchActivity(null)
    }
}