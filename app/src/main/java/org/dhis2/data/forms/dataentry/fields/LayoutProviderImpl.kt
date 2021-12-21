package org.dhis2.data.forms.dataentry.fields

import kotlin.reflect.KClass
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.age.AgeViewModel
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.data.forms.dataentry.fields.file.FileViewModel
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetViewModel
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel
import org.dhis2.data.forms.dataentry.fields.picture.PictureViewModel
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedViewModel
import org.dhis2.data.forms.dataentry.fields.visualOptionSet.MatrixOptionSetModel
import org.dhis2.form.ui.provider.LayoutProvider
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

private val layouts = mapOf<KClass<*>, Int>(
    AgeViewModel::class to R.layout.form_age_custom,
    CoordinateViewModel::class to R.layout.custom_form_coordinate,
    DisplayViewModel::class to R.layout.custom_form_display,
    FileViewModel::class to R.layout.form_button,
    OptionSetViewModel::class to R.layout.form_option_set_selector,
    OrgUnitViewModel::class to R.layout.form_org_unit,
    PictureViewModel::class to R.layout.custom_form_picture,
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
            ValueType.DATE, ValueType.TIME, ValueType.DATETIME -> R.layout.form_date_time
            ValueType.LONG_TEXT -> R.layout.form_long_text_custom
            else -> R.layout.form_edit_text_custom
        }
    }

    override fun getLayoutByValueRenderingType(
        renderingType: ValueTypeRenderingType,
        valueType: ValueType
    ): Int {
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
