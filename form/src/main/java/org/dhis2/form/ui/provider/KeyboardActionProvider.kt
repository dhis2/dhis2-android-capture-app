package org.dhis2.form.ui.provider

import org.dhis2.form.model.KeyboardActionType
import org.hisp.dhis.android.core.common.ValueType

interface KeyboardActionProvider {

    fun provideKeyboardAction(valueType: ValueType): KeyboardActionType?
}
