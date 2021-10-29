package org.dhis2.form.ui.event

import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType

interface UiEventFactory {

    fun generateEvent(
        value: String?,
        uiEventType: UiEventType?,
        renderingType: UiRenderType?
    ): RecyclerViewUiEvents?
}
