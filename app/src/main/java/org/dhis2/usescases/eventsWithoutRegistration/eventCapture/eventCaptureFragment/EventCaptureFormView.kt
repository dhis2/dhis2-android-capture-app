package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

interface EventCaptureFormView {
    fun performSaveClick()
    fun hideSaveButton()
    fun showSaveButton()
    fun onReopen()
    fun showNonEditableMessage(reason: String, canBeReOpened: Boolean)
    fun hideNonEditableMessage()
    fun displayMessage(errorMessage: String)
}
