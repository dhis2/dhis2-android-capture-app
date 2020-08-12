package org.dhis2.usescases.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventTest: BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    @Ignore
    @Test
    fun shouldDeleteEventWhenClickOnDeleteInsideSpecificEvent() {

        /**
         * Open and launch TEI
         * click on event
         * click on menu
         * click on Delete
         * accept dialog
         * check list of events, event was deleted
         * */

    }

    @Ignore
    @Test
    fun shouldShowEventDetailsWhenClickOnDetailsInsideSpecificEvent() {

        /**
         * Open and launch TEI
         * click on event
         * click on menu
         * click on Details
         * check %, OU and date
         * */

    }

}