package org.dhis2.form.ui.style

interface FormUiModelStyle {
    fun getColors(): Map<FormUiColorType, Int>
    fun getDescriptionIcon(): Int
}
