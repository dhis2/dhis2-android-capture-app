package org.dhis2.form.ui.provider

import org.dhis2.form.model.KeyboardActionType
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueType.LONG_TEXT

class KeyboardActionProviderImpl : KeyboardActionProvider {

    override fun provideKeyboardAction(valueType: ValueType) = when (valueType) {
        LONG_TEXT -> KeyboardActionType.ENTER
        else -> KeyboardActionType.NEXT
    }
}
