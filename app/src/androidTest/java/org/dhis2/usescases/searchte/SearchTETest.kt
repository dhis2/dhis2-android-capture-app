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
    fun openSearchTE() {
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
