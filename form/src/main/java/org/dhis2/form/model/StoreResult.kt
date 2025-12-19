package org.dhis2.form.model

import org.dhis2.form.data.EnrollmentRepository.Companion.ENROLLMENT_COORDINATES_UID
import org.dhis2.form.data.EnrollmentRepository.Companion.ENROLLMENT_DATE_UID
import org.dhis2.form.data.EnrollmentRepository.Companion.INCIDENT_DATE_UID
import org.dhis2.form.data.EnrollmentRepository.Companion.ORG_UNIT_UID
import org.dhis2.form.data.EnrollmentRepository.Companion.TEI_COORDINATES_UID
import org.dhis2.form.data.EventRepository.Companion.EVENT_CATEGORY_COMBO_UID
import org.dhis2.form.data.EventRepository.Companion.EVENT_COORDINATE_UID
import org.dhis2.form.data.EventRepository.Companion.EVENT_ORG_UNIT_UID
import org.dhis2.form.data.EventRepository.Companion.EVENT_REPORT_DATE_UID

data class StoreResult(
    val uid: String,
    val valueStoreResult: ValueStoreResult? = null,
    val valueStoreResultMessage: String? = null,
) {
    fun contextDataChanged() =
        uid in
            listOf(
                EVENT_REPORT_DATE_UID,
                EVENT_ORG_UNIT_UID,
                EVENT_COORDINATE_UID,
                EVENT_CATEGORY_COMBO_UID,
                ENROLLMENT_DATE_UID,
                INCIDENT_DATE_UID,
                ORG_UNIT_UID,
                TEI_COORDINATES_UID,
                ENROLLMENT_COORDINATES_UID,
            ) &&
            valueStoreResult == ValueStoreResult.VALUE_CHANGED
}
