package org.dhis2.usescases.settings

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.usescases.reservedValue.ReservedValueViewHolder
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not

fun settingsRobot(settingsRobot: SettingsRobot.() -> Unit) {
    SettingsRobot().apply {
        settingsRobot()
    }
}

class SettingsRobot: BaseRobot() {

    fun clickOnSyncData () {
        onView(withId(R.id.settingsItemData)).perform(click())
    }

    fun clickOnSyncConfiguration() {
        onView(withId(R.id.settingsItemMeta)).perform(click())
    }

    fun clickOnSyncParameters() {
        onView(withId(R.id.settingsItemParams)).perform(click())
    }

    fun clickOnReservedValues() {
        onView(withId(R.id.settingsItemValues)).perform(click())
    }

    fun clickOnOpenSyncErrorLog() {
        onView(withId(R.id.settingsItemLog)).perform(click())
    }

    fun clickOnDeleteLocalData() {
        onView(withId(R.id.settingsItemDeleteData)).perform(click())
    }

    fun clickOnResetApp () {
        onView(withId(R.id.settingsReset)).perform(click())
    }

    fun clickOnSMSSettings () {
        onView(withId(R.id.smsSettings)).perform(click())
    }

    fun checkEditPeriodIsDisableForData () {
        onView(withId(R.id.dataPeriodsNoEdition)).check(matches(withText(NOT_EDIT_TEXT)))
        onView(withId(R.id.dataPeriods)).check(matches(not(isDisplayed())))
    }

    fun checkEditPeriodIsDisableForConfiguration() {
        onView(withId(R.id.metaPeriodsNoEdition)).check(matches(withText(NOT_EDIT_TEXT)))
        onView(withId(R.id.metadataPeriods)).check(matches(not(isDisplayed())))
        onView(withId(R.id.buttonSyncMeta)).check(matches(allOf(withText("SYNC CONFIGURATION NOW"), isDisplayed())))
    }

    fun checkEditPeriodIsDisableForParameters() {
        onView(withId(R.id.parametersNoEdition)).check(matches(withText("Sync parameters are not editable")))
        onView(withId(R.id.downloadLimitScope)).check(matches(not(isDisplayed())))
        onView(withId(R.id.eventsEditText)).check(matches(not(isDisplayed())))
        onView(withId(R.id.teiEditText)).check(matches(not(isDisplayed())))
    }

    fun checkReservedValuesIsDisable() {
        onView(withId(R.id.reservedValueEditText)).check(matches(not(isDisplayed())))
    }

    fun clickOnManageReservedValues() {
        onView(withId(R.id.manageReservedValues)).perform(click())
        // MANAGE RESERVED VALUES
    }

    fun clickOnRefill(position: Int) {
        /*onView(withId(R.id.recycler))
                .check(matches(allOf(atPosition(position, hasDescendant(withId(R.id.refill))))))
                .perform(click())
                .perform(actionOnItemAtPosition<ReservedValueViewHolder>(position, click()))*/
        onView(withId(R.id.recycler))
                .perform(actionOnItemAtPosition<ReservedValueViewHolder>(position, ClickRefillButton()))
    }

    fun checkReservedValuesWasRefill(position: Int) {
        onView(withId(R.id.recycler)).check(matches(allOf(
                atPosition(position, hasDescendant(withText("100 reserved values left"))))))
    }

    fun checkLogViewIsDisplayed() {
        /*onView(withId(R.id.errorRecycler)).check(matches(allOf(
                isDisplayed(), not(isNotEmpty())
        )))*/
        onView(withId(R.id.errorRecycler)).check(matches(isDisplayed()))
    }

    fun clickOnAcceptDelete() {
        onView(withId(R.id.deleteDataButton)).perform(click())
    }

    fun clickOnAcceptDialog() {
        onView(withText(R.string.wipe_data_ok)).perform(click())
    }

    fun checkSnackBarIsShown() {
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Delete local data finished successfully.")))
    }

    fun checkOnAcceptReset() {
        onView(withId(R.id.resetButton)).perform(click())
    }

    fun checkGatewayNumberFieldIsDisable() {
        onView(withId(R.id.settings_sms_receiver))
                //.perform(scrollTo())
                .check(matches(allOf(isDisplayed())))
    }

    fun checkSMSSubmissionIsEnable() {
        onView(withId(R.id.settings_sms_response_wait_switch))
                .check(matches(isEnabled()))
    }

    companion object {
        const val NOT_EDIT_TEXT = "Syncing period is not editable"
        const val SYNC_DATA = "SYNC DATA NOW"
    }

}