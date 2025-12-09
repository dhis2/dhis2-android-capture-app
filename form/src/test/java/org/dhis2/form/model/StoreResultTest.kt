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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreResultTest {
    @Test
    fun `contextDataChanged returns true for EVENT_REPORT_DATE_UID with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = EVENT_REPORT_DATE_UID,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertTrue(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns true for EVENT_ORG_UNIT_UID with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = EVENT_ORG_UNIT_UID,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertTrue(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns true for EVENT_COORDINATE_UID with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = EVENT_COORDINATE_UID,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertTrue(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns true for EVENT_CATEGORY_COMBO_UID with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = EVENT_CATEGORY_COMBO_UID,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertTrue(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns true for ENROLLMENT_DATE_UID with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = ENROLLMENT_DATE_UID,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertTrue(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns true for INCIDENT_DATE_UID with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = INCIDENT_DATE_UID,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertTrue(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns true for ORG_UNIT_UID with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = ORG_UNIT_UID,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertTrue(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns true for TEI_COORDINATES_UID with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = TEI_COORDINATES_UID,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertTrue(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns true for ENROLLMENT_COORDINATES_UID with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = ENROLLMENT_COORDINATES_UID,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertTrue(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns false for context uid with VALUE_HAS_NOT_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = EVENT_REPORT_DATE_UID,
                valueStoreResult = ValueStoreResult.VALUE_HAS_NOT_CHANGED,
            )

        assertFalse(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns false for context uid with null valueStoreResult`() {
        val storeResult =
            StoreResult(
                uid = EVENT_REPORT_DATE_UID,
                valueStoreResult = null,
            )

        assertFalse(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns false for regular data element uid with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = "regularDataElementUid",
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertFalse(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns false for regular attribute uid with VALUE_CHANGED`() {
        val storeResult =
            StoreResult(
                uid = "attributeUid123",
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )

        assertFalse(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns false for context uid with ERROR_UPDATING_VALUE`() {
        val storeResult =
            StoreResult(
                uid = ENROLLMENT_DATE_UID,
                valueStoreResult = ValueStoreResult.ERROR_UPDATING_VALUE,
            )

        assertFalse(storeResult.contextDataChanged())
    }

    @Test
    fun `contextDataChanged returns false for context uid with VALUE_NOT_UNIQUE`() {
        val storeResult =
            StoreResult(
                uid = ORG_UNIT_UID,
                valueStoreResult = ValueStoreResult.VALUE_NOT_UNIQUE,
            )

        assertFalse(storeResult.contextDataChanged())
    }
}
