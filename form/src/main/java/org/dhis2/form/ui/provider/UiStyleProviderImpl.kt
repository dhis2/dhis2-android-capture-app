package org.dhis2.form.ui.provider

import org.dhis2.form.ui.style.BasicFormUiModelStyle
import org.dhis2.form.ui.style.FormUiColorFactory
import org.dhis2.form.ui.style.FormUiModelStyle
import org.dhis2.form.ui.style.LongTextDecorator
import org.hisp.dhis.android.core.common.ValueType

class UiStyleProviderImpl(
    private val colorFactory: FormUiColorFactory,
    private val longTextColorFactory: FormUiColorFactory,
    private val actionIconClickable: Boolean
) : UiStyleProvider {
    override fun provideStyle(valueType: ValueType): FormUiModelStyle {
        val style: FormUiModelStyle =
            BasicFormUiModelStyle(colorFactory, valueType, actionIconClickable)

        return if (valueType === ValueType.LONG_TEXT) {
            LongTextDecorator(style, longTextColorFactory)
        } else {
            style
        }
    }
}
