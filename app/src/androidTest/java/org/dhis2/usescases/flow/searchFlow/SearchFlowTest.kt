package org.dhis2.usescases.flow.searchFlow

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.flow.teiFlow.entity.DateRegistrationUIModel
import org.dhis2.usescases.flow.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.flow.teiFlow.teiFlowRobot
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchFlowTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dateRegistration = createFirstSpecificDate()
    private val dateEnrollment = createEnrollmentDate()

    @Test
    fun shouldCreateTEIAndFilterByEnrollment() {
        setDatePicker()
        val registerTEIDetails = createRegisterTEI()
        val enrollmentStatus = context.getString(R.string.filters_title_enrollment_status)
            .format(
                context.resources.getQuantityString(R.plurals.enrollment, 1)
                    .capitalize(Locale.current)
            )
        val filterCounter = "1"
        val filterTotalCount = "2"
        prepareWomanProgrammeIntentAndLaunchActivity(rule)

        teiFlowRobot(composeTestRule) {
            registerTEI(registerTEIDetails)
            pressBack()
        }

        searchFlowRobot {
            filterByOpenEnrollmentStatus(enrollmentStatus)
            checkSearchCounters(filterCounter, enrollmentStatus, filterTotalCount)
            checkTEIEnrollment()
        }
    }

    private fun createRegisterTEI() = RegisterTEIUIModel(
        NAME,
        LASTNAME,
        dateRegistration,
        dateEnrollment
    )

    private fun createFirstSpecificDate() = DateRegistrationUIModel(
        2020,
        6,
        30
    )

    private fun createEnrollmentDate() = DateRegistrationUIModel(
        2020,
        8,
        30
    )

    private fun prepareWomanProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
        Intent().apply {
            putExtra(PROGRAM_UID, WOMAN_PROGRAM_UID_VALUE)
            putExtra(TE_TYPE, WOMAN_TE_TYPE_VALUE)
        }.also { ruleSearch.launchActivity(it) }
    }

    companion object {
        const val PROGRAM_UID = "PROGRAM_UID"
        const val TE_TYPE = "TRACKED_ENTITY_UID"
        const val WOMAN_PROGRAM_UID_VALUE = "uy2gU8kT1jF"
        const val WOMAN_TE_TYPE_VALUE = "nEenWmSyUEp"

        const val NAME = "NADIA"
        const val LASTNAME = "BELAUNDE"
    }
}
