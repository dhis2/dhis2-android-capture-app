package org.dhis2.form.model

import org.dhis2.commons.data.EntryMode

sealed class FormRepositoryRecords(
    val recordUid: String?,
    val entryMode: EntryMode? = null,
    val allowMandatoryFields: Boolean = true,
    val isBackgroundTransparent: Boolean = true,
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
) : FormRepositoryRecords(
    recordUid = eventUid,
    entryMode = EntryMode.DE,
)

class SearchRecords(
    val programUid: String?,
    val teiTypeUid: String,
    val currentSearchValues: Map<String, String>,
) : FormRepositoryRecords(
    recordUid = programUid,
    allowMandatoryFields = false,
    isBackgroundTransparent = false,
)
