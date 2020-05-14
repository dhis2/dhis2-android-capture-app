package org.dhis2.usescases.enrollment

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.SearchTETest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnrollmentTest : BaseTest(){

    @get:Rule
    val ruleSearch = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @get:Rule
    val ruleEnrollment = ActivityTestRule(EnrollmentActivity::class.java, false, false)

    override fun setUp() {
        super.setUp()
        //init robot
    }

    @Test
    fun openEnrollment() {

    }

    private fun prepareSearchIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(SearchTETest.CHILD_PROGRAM_UID, SearchTETest.CHILD_PROGRAM_UID_VALUE)
            putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
        }.also { ruleSearch.launchActivity(it) }
    }
}