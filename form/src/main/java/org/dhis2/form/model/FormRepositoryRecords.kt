package org.dhis2.form.model

import org.dhis2.commons.data.EntryMode

sealed class FormRepositoryRecords(
    val recordUid: String, // has to be enrollmentUid or eventUid or programUid  for search
    val entryMode: EntryMode? = null,
    val allowMandatoryFields: Boolean = true,
    val isBackgroundTransparent: Boolean = true
) : java.io.Serializable

class EnrollmentRecords(
    recordUid: String,
    val enrollmentMode: EnrollmentMode
) : FormRepositoryRecords(
    recordUid = recordUid,
    entryMode = EntryMode.ATTR
)

class EventRecords(
    recordUid: String
) : FormRepositoryRecords(
    recordUid = recordUid,
    entryMode = EntryMode.DE
)

class SearchRecords(
    recordUid: String,
    val teiTypeUid: String,
    val currentSearchValues: Map<String, String>
) : FormRepositoryRecords(
    recordUid = recordUid,
    allowMandatoryFields = false,
    isBackgroundTransparent = false
)
