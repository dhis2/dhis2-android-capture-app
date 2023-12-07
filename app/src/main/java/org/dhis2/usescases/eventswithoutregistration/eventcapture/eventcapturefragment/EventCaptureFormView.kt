package org.dhis2.usescases.eventswithoutregistration.eventcapture.eventcapturefragment

interface EventCaptureFormView {
    fun performSaveClick()
    fun hideSaveButton()
    fun showSaveButton()
    fun onReopen()
}
