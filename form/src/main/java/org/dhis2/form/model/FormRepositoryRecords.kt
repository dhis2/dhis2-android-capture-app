package org.dhis2.form.model

import org.dhis2.commons.data.EntryMode

sealed class FormRepositoryRecords(
    val recordUid: String,
    val entryMode: EntryMode,
) : java.io.Serializable

class EnrollmentRecords(
    val enrollmentUid: String,
    val enrollmentMode: EnrollmentMode,
) : FormRepositoryRecords(
    recordUid = enrollmentUid,
    entryMode = EntryMode.ATTR,
)

class EventRecords(
    val eventUid: String,
    val eventMode: EventMode,
) : FormRepositoryRecords(
    recordUid = eventUid,
    entryMode = EntryMode.DE,
)
