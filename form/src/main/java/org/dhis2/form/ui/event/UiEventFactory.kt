package org.dhis2.form.ui.event

import org.dhis2.form.model.UiEventType

interface UiEventFactory {

    fun generateEvent(
        value: String?,
        uiEventType: UiEventType?
    ): RecyclerViewUiEvents?
}
