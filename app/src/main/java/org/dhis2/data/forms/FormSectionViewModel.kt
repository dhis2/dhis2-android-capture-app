package org.dhis2.data.forms

data class FormSectionViewModel(
    // uid of Event or Enrollment
    val uid: String,
    val sectionUid: String?,
    val label: String?,
    val renderType: String?,
)
