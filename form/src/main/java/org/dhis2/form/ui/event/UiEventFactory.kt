package org.dhis2.form.ui.event

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType

interface UiEventFactory {

    fun generateEvent(
        value: String?,
        uiEventType: UiEventType? = null,
        renderingType: UiRenderType? = null,
        fieldUiModel: FieldUiModel,
    ): RecyclerViewUiEvents?
}
