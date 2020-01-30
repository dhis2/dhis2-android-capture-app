package org.dhis2.usescases.searchte

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEViewHolder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchTETest : BaseTest(){

    private lateinit var searchTETest: SearchTETest

    @get:Rule
    val rule = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @Test
    fun openSearchTE() {
        Intent().apply {
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(CHILD_TE_TYPE, CHILD_TE_TYPE_VALUE)
        }.also { rule.launchActivity(it) }

        //Working on it
   //     onView(withId(R.id.scrollView))
   //             .perform(RecyclerViewActions.actionOnItemAtPosition<SearchTEViewHolder>(0, click()))

       // onView(withRecyclerView(R.id.scrollView)).perform(RecyclerViewActions.scrollToPosition<SearchTEViewHolder>(15))
    //    RecyclerViewActions.scrollToPosition<SearchTEViewHolder>(15)
        Thread.sleep(2000)
    }

    companion object{
        const val CHILD_PROGRAM_UID = "PROGRAM_UID"
        const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"

        const val CHILD_TE_TYPE_VALUE = "nEenWmSyUEp"
        const val CHILD_TE_TYPE = "TRACKED_ENTITY_UID"
    }
}