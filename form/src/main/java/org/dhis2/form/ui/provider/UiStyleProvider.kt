package org.dhis2.form.ui.provider

import org.dhis2.form.ui.style.FormUiModelStyle
import org.hisp.dhis.android.core.common.ValueType

interface UiStyleProvider {

    fun provideStyle(valueType: ValueType): FormUiModelStyle
}
