package org.dhis2.form.ui.event

interface UiEventFactory {

    fun generateEvent(value: String?): RecyclerViewUiEvents?
}
