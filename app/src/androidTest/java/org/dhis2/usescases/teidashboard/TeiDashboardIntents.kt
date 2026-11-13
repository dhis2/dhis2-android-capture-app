package org.dhis2.usescases.teidashboard

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.dhis2.LazyActivityScenarioRule
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.SearchTETest
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus

private const val PROGRAM_UID = "PROGRAM_UID"
private const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"
private const val TB_PROGRAM_UID = "ur1Edk5Oe2n"
private const val RMNCH_PROGRAM_UID = "WSGAb5XwJ3Y"
private const val RMNCH_ORG_UNIT_UID = "WAjjFMDJKcx"
const val DEFAULT_CATEGORY_COMBO_NAME = "default"
private const val TEI_UID = "TEI_UID"
private const val ENROLLMENT_UID = "ENROLLMENT_UID"
private const val TEI_UID_VALUE_COMPLETED = "vOxUH373fy5"
private const val ENROLLMENT_VALUE_COMPLETED = "KpknKHptul0"
private const val TEI_UID_VALUE_OPENED = "Pqv3LrNECkn"
private const val ENROLLMENT_VALUE_OPENED = "clL63cPdk86"
private const val TEI_UID_VALUE_OPEN_REFERRAL = "G1afLIEKt8A"
private const val ENROLLMENT_VALUE_OPEN_REFERRAL = "uzz6zvuRyjn"
private const val TEI_UID_VALUE_OPEN_TO_COMPLETE = "qx4yw1EuxmW"
private const val ENROLLMENT_VALUE_OPEN_TO_COMPLETE = "ld0PCa4FGfq"
private const val TEI_UID_VALUE_TO_SCHEDULE = "uh47DXf1St9"
private const val ENROLLMENT_VALUE_TO_SCHEDULE = "iSNBeFcHO0X"
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

data class EnrollmentWithAOC(
    val programUid: String,
    val teiUid: String,
    val enrollmentUid: String,
    val categoryComboName: String,
    val categoryOptionComboName: String,
)

/**
 * Attributes an active WHO RMNCH Tracker enrollment — whose program uses the non-default
 * enrollment category combo "HIV age+gender" — to one of that combo's option combos.
 * The demo enrollments all carry the default option combo, so the fixture is seeded here
 * and resolved at runtime instead of relying on a hardcoded enrollment.
 */
fun attributeRmnchEnrollmentToNonDefaultAOC(): EnrollmentWithAOC {
    val d2 = D2Manager.getD2()
    val program = d2.programModule().programs().uid(RMNCH_PROGRAM_UID).blockingGet()!!
    val categoryCombo =
        d2
            .categoryModule()
            .categoryCombos()
            .uid(program.enrollmentCategoryCombo().uid())
            .blockingGet()!!
    val categoryOptionCombo =
        d2
            .categoryModule()
            .categoryOptionCombos()
            .byCategoryComboUid()
            .eq(categoryCombo.uid())
            .blockingGet()
            .first()
    val enrollment =
        d2
            .enrollmentModule()
            .enrollments()
            .byProgram()
            .eq(RMNCH_PROGRAM_UID)
            .byOrganisationUnit()
            .eq(RMNCH_ORG_UNIT_UID)
            .byStatus()
            .eq(EnrollmentStatus.ACTIVE)
            .byDeleted()
            .eq(false)
            .blockingGet()
            .first()

    d2
        .enrollmentModule()
        .enrollments()
        .uid(enrollment.uid())
        .setAttributeOptionComboUid(categoryOptionCombo.uid())

    return EnrollmentWithAOC(
        programUid = RMNCH_PROGRAM_UID,
        teiUid = enrollment.trackedEntityInstance()!!,
        enrollmentUid = enrollment.uid(),
        categoryComboName = categoryCombo.displayName()!!,
        categoryOptionComboName = categoryOptionCombo.displayName()!!,
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

