package org.dhis2.usescases.eventswithoutregistration.eventCapture.eventCaptureFragment

interface EventCaptureFormView {
    fun performSaveClick()
    fun hideSaveButton()
    fun showSaveButton()
    fun onReopen()
}
