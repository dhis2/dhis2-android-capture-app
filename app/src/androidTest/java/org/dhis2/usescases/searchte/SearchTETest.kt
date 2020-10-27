package org.dhis2.usescases.searchte

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.flow.teiFlow.entity.DateRegistrationUIModel
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.entity.DisplayListFieldsUIModel
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchTETest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @Test
    fun shouldSuccessfullySearchByName() {
        val firstName = "Tim"
        val firstNamePosition = 0
        val filterCount = "1"

        prepareChildProgrammeIntentAndLaunchActivity(rule)
        
        searchTeiRobot {
            typeAttributeAtPosition(firstName, firstNamePosition)
            clickOnFab()
            checkFilterCount(filterCount)
            closeSearchForm()
            checkListOfSearchTEI(firstName, "")
        }
    }

    @Test
    fun shouldShowErrorWhenCanNotFindSearchResult() {
        val firstName = "asdssds"
        val firstNamePosition = 1
        val filterCount = "1"
        val noResultMessage = context.getString(R.string.search_criteria_not_met).replace("%s","Person")

        prepareTestProgramRulesProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            typeAttributeAtPosition(firstName, firstNamePosition)
            clickOnFab()
            checkFilterCount(filterCount)
            closeSearchForm()
            checkNoSearchResult(firstName, noResultMessage)
        }
    }

    @Test
    fun shouldSuccessfullySearchUsingMoreThanOneField() {
        val firstName = "Anna"
        val firstNamePosition = 0
        val lastName = "Jones"
        val lastNamePosition = 1
        val filterCount = "2"

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            typeAttributeAtPosition(firstName, firstNamePosition)
            typeAttributeAtPosition(lastName, lastNamePosition)
            clickOnFab()
            checkFilterCount(filterCount)
            closeSearchForm()
            checkListOfSearchTEI(firstName, lastName)
        }
    }

    @Test
    fun shouldSuccessfullyChangeBetweenPrograms() {
        val tbProgram = "TB program"

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnProgramSpinner()
            selectAProgram(tbProgram)
            checkProgramHasChanged(tbProgram)
        }
    }

    @Test
    fun shouldCheckDisplayInList() {
        val birthdaySearch = createDateOfBirthSearch()
        val displayInListData = createDisplayListFields()
        val namePosition = 0
        val lastNamePosition = 1
        val filterCount = "3"

        prepareTestAdultWomanProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            typeAttributeAtPosition(displayInListData.name, namePosition)
            typeAttributeAtPosition(displayInListData.lastName, lastNamePosition)
            clickOnDateField()
            selectSpecificDate(birthdaySearch.year, birthdaySearch.month, birthdaySearch.day)
            acceptDate()
            clickOnFab()
            checkFilterCount(filterCount)
            closeSearchForm()
            checkFieldsFromDisplayList(displayInListData)
        }
    }

    @Test
    fun shouldSuccessfullyFilterByEnrollmentStatusCompleted() {
        val enrollmentStatusFilter = context.getString(R.string.filters_title_enrollment_status)

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnFilter()
            clickOnFilterBy(enrollmentStatusFilter)
            clickOnFilterCancelledOption()
            closeSearchForm()
            checkTEIsAreCancelled()
        }
    }

    @Test
    fun shouldSuccessfullyFilterByEventStatusOverdue() {
        val eventStatusFilter = context.getString(R.string.filters_title_event_status)
        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnFilter()
            clickOnFilterBy(eventStatusFilter)
            clickOnFilterOverdueOption()
            closeFilterRowAtField(eventStatusFilter)
            closeSearchForm()
            checkEventsAreOverdue()
        }
    }

    @Test
    fun shouldSuccessfullyFilterByOrgUnitAndUseSort() {
        val orgUnitFilter = "ORG. UNIT"
        val orgUnitNgelehun = "Ngelehun CHC"
        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnFilter()
            clickOnFilterBy(orgUnitFilter)
            clickOnSortByField(orgUnitFilter)
            typeOrgUnitField(orgUnitNgelehun)
            closeSearchForm()
            checkTEIWithOrgUnit(orgUnitNgelehun)
        }
    }

    @Test
    fun shouldSuccessfullyFilterBySync() {
        val teiName = "Frank"
        val teiLastName = "Fjordsen"
        val syncFilter = "SYNC"
        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            closeSearchForm()
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuReOpen()
            checkUnlockIconIsDisplay()
            pressBack()
        }

        searchTeiRobot {
            clickOnFilter()
            clickOnFilterBy(syncFilter)
            clickOnNotSync()
            closeSearchForm()
            checkTEINotSync()
        }
    }

    private fun createDateOfBirthSearch() = DateRegistrationUIModel(
        2001,
        1,
        1
    )

    private fun createDisplayListFields() = DisplayListFieldsUIModel(
        "Sarah",
        "Thompson",
        "2001-01-01",
        "sarah@gmail.com",
        "Main street 1",
        "56",
        "167"
    )

    companion object {
        const val PROGRAM_UID = "PROGRAM_UID"
        const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"

        const val CHILD_TE_TYPE_VALUE = "nEenWmSyUEp"
        const val CHILD_TE_TYPE = "TRACKED_ENTITY_UID"
    }
}
