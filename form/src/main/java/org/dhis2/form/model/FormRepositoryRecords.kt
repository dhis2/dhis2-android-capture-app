package org.dhis2.form.model

sealed class FormRepositoryRecords(
    val recordUid: String, // has to be enrollmentUid or eventUid or programUid  for search
    val allowMandatoryFields: Boolean = true,
    val isBackgroundTransparent: Boolean = true
)

class EnrollmentRecords(
    recordUid: String,
    val enrollmentMode: EnrollmentMode
) : FormRepositoryRecords(recordUid)

class EventRecords(
    recordUid: String
) : FormRepositoryRecords(recordUid)

class SearchRecords(
    recordUid: String,
    val teiTypeUid: String,
    val currentSearchValues: Map<String, String>
) : FormRepositoryRecords(recordUid, false, false)
