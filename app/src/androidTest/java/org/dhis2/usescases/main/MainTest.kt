package org.dhis2.usescases.main

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.login.loginRobot
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
        enableIntents()
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnLogout()
            checkLogInIsLaunched()
        }

        loginRobot {
            checkUsernameFieldIsClear()
            checkPasswordFieldIsClear()
        }
    }

    //Create tests

//    @Test
//    fun loginButtonShouldBeDisplayedWhenAllFieldsAreFilled2() {
    //    startTeiDashboardActivity()
     //   Thread.sleep(2000)

        //  onView(withId(R.id.login)).check(matches(not(isDisplayed())))
        //  onView(withId(R.id.server_url_edit)).perform(replaceText(TEST_URL), pressImeActionButton())
        //  onView(withId(R.id.user_name_edit)).perform(
        //      replaceText(TEST_USERNAME),
        //      pressImeActionButton()
        //  )
        //  onView(withId(R.id.user_pass_edit)).perform(
        //     replaceText(TEST_USERNAME),
        //     pressImeActionButton()
        // )

        //    onView(withId(R.id.login)).check(matches(isDisplayed()))
 //   }


    fun startActivity(){
        rule.launchActivity(null)
    }
}