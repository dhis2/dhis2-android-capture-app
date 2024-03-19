package org.dhis2.usescases.teidashboard

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.dhis2.LazyActivityScenarioRule
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.SearchTETest
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity

private const val PROGRAM_UID = "PROGRAM_UID"
private const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"
private const val TB_PROGRAM_UID = "ur1Edk5Oe2n"
private const val TEI_UID = "TEI_UID"
private const val ENROLLMENT_UID = "ENROLLMENT_UID"
private const val TEI_UID_VALUE_COMPLETED = "vOxUH373fy5"
private const val ENROLLMENT_VALUE_COMPLETED = "KpknKHptul0"
private const val TEI_UID_VALUE_OPENED = "Pqv3LrNECkn"
private const val ENROLLMENT_VALUE_OPENED = "clL63cPdk86"
private const val TEI_UID_VALUE_OPENED_FULL = "r2FEXpX6ize"
private const val ENROLLMENT_VALUE_OPENED_FULL = "Z6yN122z3Fw"
private const val TEI_UID_VALUE_OPEN_REFERRAL = "Fs6QyeOdDA3"
private const val ENROLLMENT_VALUE_OPEN_REFERRAL = "oxX5GjRALXY"
private const val TEI_UID_VALUE_OPEN_TO_COMPLETE = "qx4yw1EuxmW"
private const val ENROLLMENT_VALUE_OPEN_TO_COMPLETE = "ld0PCa4FGfq"
private const val TEI_UID_VALUE_WITH_NOTE = "UtDZmrX5lSd"
private const val ENROLLMENT_VALUE_WITH_NOTE = "zDZ9lZCj2aa"
private const val TEI_UID_VALUE_TO_DELETE = "SHnmavBQu72"
private const val ENROLLMENT_VALUE_TO_DELETE = "xkOTz2PrM5Z"
private const val TEI_UID_VALUE_TO_SCHEDULE = "uh47DXf1St9"
private const val ENROLLMENT_VALUE_TO_SCHEDULE = "iSNBeFcHO0X"
private const val TEI_UID_VALUE_TO_CREATE_EVENT = "xWGaOQL0INS"
private const val ENROLLMENT_VALUE_TO_CREATE_EVENT = "NGFp6txgfn8"
private const val TEI_UID_VALUE_TO_EDIT_EVENT = "PQfMcpmXeFE"
private const val ENROLLMENT_VALUE_TO_EDIT_EVENT = "Yf47yST5FF2"
private const val TEI_UID_VALUE_TO_ENROLL = "tIJu6iqQxNV"
private const val ENROLLMENT_VALUE_TO_ENROLL = "CCBLMntFuzb"
private const val TEI_UID_VALUE_ANALYTICS = "wsk89u7zquT"
private const val ENROLLMENT_UID_VALUE_ANALYTICS = "ITyaPVATEwc"

fun prepareTeiCompletedProgrammeAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        CHILD_PROGRAM_UID_VALUE,
        TEI_UID_VALUE_COMPLETED,
        ENROLLMENT_VALUE_COMPLETED,
        rule
    )
}

fun prepareTeiOpenedForReferralProgrammeAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        TB_PROGRAM_UID,
        TEI_UID_VALUE_OPEN_REFERRAL,
        ENROLLMENT_VALUE_OPEN_REFERRAL,
        rule
    )
}

fun prepareTeiOpenedProgrammeAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        CHILD_PROGRAM_UID_VALUE,
        TEI_UID_VALUE_OPENED,
        ENROLLMENT_VALUE_OPENED,
        rule
    )
}

fun prepareTeiOpenedForCompleteProgrammeAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        CHILD_PROGRAM_UID_VALUE,
        TEI_UID_VALUE_OPEN_TO_COMPLETE,
        ENROLLMENT_VALUE_OPEN_TO_COMPLETE,
        rule
    )
}

fun prepareTeiWithExistingNoteAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        CHILD_PROGRAM_UID_VALUE,
        TEI_UID_VALUE_WITH_NOTE,
        ENROLLMENT_VALUE_WITH_NOTE,
        rule
    )
}

fun prepareTeiOpenedWithFullEventsAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        CHILD_PROGRAM_UID_VALUE,
        TEI_UID_VALUE_OPENED_FULL,
        ENROLLMENT_VALUE_OPENED_FULL,
        rule
    )
}

fun prepareTeiToDeleteAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        CHILD_PROGRAM_UID_VALUE,
        TEI_UID_VALUE_TO_DELETE,
        ENROLLMENT_VALUE_TO_DELETE,
        rule
    )
}

fun prepareTeiOpenedWithNoPreviousEventProgrammeAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        TB_PROGRAM_UID,
        TEI_UID_VALUE_TO_SCHEDULE,
        ENROLLMENT_VALUE_TO_SCHEDULE,
        rule
    )
}

fun prepareTeiToCreateANewEventAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        TB_PROGRAM_UID,
        TEI_UID_VALUE_TO_CREATE_EVENT,
        ENROLLMENT_VALUE_TO_CREATE_EVENT,
        rule
    )
}

fun prepareTeiOpenedToEditAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        TB_PROGRAM_UID,
        TEI_UID_VALUE_TO_EDIT_EVENT,
        ENROLLMENT_VALUE_TO_EDIT_EVENT,
        rule
    )
}

fun prepareTeiToEnrollToOtherProgramAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        CHILD_PROGRAM_UID_VALUE,
        TEI_UID_VALUE_TO_ENROLL,
        ENROLLMENT_VALUE_TO_ENROLL,
        rule
    )
}

fun prepareTeiForAnalyticsAndLaunchActivity(
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    startTeiDashboardActivity(
        TB_PROGRAM_UID,
        TEI_UID_VALUE_ANALYTICS,
        ENROLLMENT_UID_VALUE_ANALYTICS,
        rule
    )
}

fun startTeiDashboardActivity(
    programUID: String?,
    teiUID: String,
    enrollmentUID: String?,
    rule: LazyActivityScenarioRule<TeiDashboardMobileActivity>
) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        TeiDashboardMobileActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, programUID)
        putExtra(TEI_UID, teiUID)
        putExtra(ENROLLMENT_UID, enrollmentUID)
    }.also { rule.launch(it) }
}

fun prepareChildProgrammeIntentAndLaunchActivity(
    ruleSearch: LazyActivityScenarioRule<SearchTEActivity>
) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        SearchTEActivity::class.java,
    ).apply {
        putExtra(SearchTETest.PROGRAM_UID, SearchTETest.CHILD_PROGRAM_UID_VALUE)
        putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launch(it) }
}

