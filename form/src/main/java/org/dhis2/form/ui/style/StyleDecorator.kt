package org.dhis2.form.ui.style

open class StyleDecorator(protected var style: FormUiModelStyle) : FormUiModelStyle {
    override fun getColors(): Map<FormUiColorType, Int> {
        return style.getColors()
    }

    override fun getDescriptionIcon(): Int {
        return getDescriptionIcon()
    }
}
