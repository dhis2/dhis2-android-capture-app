package org.dhis2.usescases.enrollment

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.teidashboard.TeiDashboardTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnrollmentTest : BaseTest(){

    @get:Rule
    val rule = ActivityTestRule(EnrollmentActivity::class.java, false, false)

    override fun setUp() {
        super.setUp()
        //init robot
    }

    @Test
    fun openEnrollment() {
        prepareEnrollmentIntentAndLaunchActivity()
        Thread.sleep(2000)
    }

    private fun prepareEnrollmentIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(ENROLLMENT_UID_EXTRA, ENROLLMENT_UID_EXTRA_VALUE)
            putExtra(PROGRAM_UID_EXTRA, PROGRAM_UID_EXTRA_VALUE)
            putExtra(MODE_EXTRA, EnrollmentActivity.EnrollmentMode.NEW)
        }.also { rule.launchActivity(it) }
    }

    companion object {
        const val ENROLLMENT_UID_EXTRA = "ENROLLMENT_UID_EXTRA"
        const val ENROLLMENT_UID_EXTRA_VALUE = "GqLKN5AKh0M"
        const val PROGRAM_UID_EXTRA = "PROGRAM_UID_EXTRA"
        const val PROGRAM_UID_EXTRA_VALUE = "IpHINAT79UW"
        const val MODE_EXTRA = "MODE_EXTRA"
    }
}