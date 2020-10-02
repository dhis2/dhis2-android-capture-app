package org.dhis2.usescases.searchte

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchTETest : BaseTest() {

    private lateinit var searchTETest: SearchTETest

    @get:Rule
    val rule = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @Test
    fun shouldSuccessfullySearchByName() {
        val firstName = "Anna"
        val firstNamePosition = 0
        val filterCount = "1"

        prepareChildProgrammeIntentAndLaunchActivity()

        searchTeiRobot {
            searchByPosition(firstName, firstNamePosition)
            clickOnFab()
            checkFilterCount(filterCount)
            closeSearchForm()
            checkListOfSearchTEI(firstName)
        }
    }

    @Test
    fun shouldShowErrorWhenCanNotFindSearchResult() {
        /**
         * launch program
         * search using a weird search word
         * click on fab
         * check filter count 1
         * check empty recycler
         * check result message
         * */


    }

    @Test
    fun shouldSuccessfullySearchUsingMoreThanOneField() {
        /**
         * launch program
         * choose one search
         * add another search
         * click on search
         * check search count 2
         * check result
         * */
    }

    @Test
    fun shouldSuccessfullyChangeBetweenPrograms() {
        /**
         * launch child program
         * click on spinner and select another program (TB program)
         * check it does not crash
         * */

        prepareChildProgrammeIntentAndLaunchActivity()

        searchTeiRobot {
            Thread.sleep(10000)
            clickOnProgramSpinner() //it sends me error action will not be performed (90 percent of the view)
            Thread.sleep(1000)
            //selectAProgram("TB program")
        }
    }

    @Test
    fun shouldSuccessfullyFilterBySync() {
        prepareChildProgrammeIntentAndLaunchActivity()

        searchTeiRobot {
            /*clickOnSearchFilter()
            clickOnFilterByName("SYNC")*/
            //clickOnFilterByName("Synced")
            closeSearchForm()
        }
    }

    private fun prepareChildProgrammeIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(CHILD_TE_TYPE, CHILD_TE_TYPE_VALUE)
        }.also { rule.launchActivity(it) }
    }

    companion object {
        const val CHILD_PROGRAM_UID = "PROGRAM_UID"
        const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"

        const val CHILD_TE_TYPE_VALUE = "nEenWmSyUEp"
        const val CHILD_TE_TYPE = "TRACKED_ENTITY_UID"
    }
}
