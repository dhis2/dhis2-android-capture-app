package org.dhis2.form.ui.style

interface FormUiColorFactory {
    fun getBasicColors(): Map<FormUiColorType, Int>
}
