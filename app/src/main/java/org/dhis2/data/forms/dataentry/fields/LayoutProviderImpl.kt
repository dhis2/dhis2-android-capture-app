package org.dhis2.data.forms.dataentry.fields

import kotlin.reflect.KClass
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.data.forms.dataentry.fields.file.FileViewModel
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedViewModel
import org.dhis2.data.forms.dataentry.fields.visualOptionSet.MatrixOptionSetModel
import org.dhis2.form.ui.provider.LayoutProvider
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

private val layouts = mapOf<KClass<*>, Int>(
    DisplayViewModel::class to R.layout.custom_form_display,
    FileViewModel::class to R.layout.form_button,
    ScanTextViewModel::class to R.layout.form_scan,
    SectionViewModel::class to R.layout.form_section,
    SpinnerViewModel::class to R.layout.form_option_set_spinner,
    UnsupportedViewModel::class to R.layout.form_unsupported,
    MatrixOptionSetModel::class to R.layout.matrix_option_set
)

class LayoutProviderImpl : LayoutProvider {

    override fun getLayoutByModel(modelClass: KClass<*>): Int {
        return layouts[modelClass]!!
    }

    override fun getLayoutByValueType(valueType: ValueType): Int {
        return when (valueType) {
            ValueType.AGE -> R.layout.form_age_custom
            ValueType.DATE, ValueType.TIME, ValueType.DATETIME -> R.layout.form_date_time
            ValueType.LONG_TEXT -> R.layout.form_long_text_custom
            ValueType.ORGANISATION_UNIT -> R.layout.form_org_unit
            ValueType.COORDINATE -> R.layout.form_coordinate_custom
            ValueType.IMAGE -> R.layout.form_picture
            else -> R.layout.form_edit_text_custom
        }
    }

    override fun getLayoutByValueRenderingType(
        renderingType: ValueTypeRenderingType,
        valueType: ValueType
    ): Int {
        if (valueType == ValueType.TEXT) {
            return when (renderingType) {
                ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS,
                ValueTypeRenderingType.VERTICAL_RADIOBUTTONS,
                ValueTypeRenderingType.HORIZONTAL_CHECKBOXES,
                ValueTypeRenderingType.VERTICAL_CHECKBOXES -> R.layout.form_option_set_selector
                else -> R.layout.form_edit_text_custom
            }
        }
        if (renderingType == ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS ||
            renderingType == ValueTypeRenderingType.DEFAULT ||
            renderingType == ValueTypeRenderingType.VERTICAL_RADIOBUTTONS ||
            renderingType == ValueTypeRenderingType.TOGGLE &&
            valueType !== ValueType.TRUE_ONLY
        ) {
            return R.layout.form_radio_button_horizontal
        } else if (renderingType == ValueTypeRenderingType.HORIZONTAL_CHECKBOXES ||
            renderingType == ValueTypeRenderingType.VERTICAL_CHECKBOXES
        ) {
            return R.layout.form_check_button
        } else if (renderingType == ValueTypeRenderingType.TOGGLE &&
            valueType === ValueType.TRUE_ONLY
        ) {
            return R.layout.form_toggle
        }
        return R.layout.form_yes_no
    }
}
