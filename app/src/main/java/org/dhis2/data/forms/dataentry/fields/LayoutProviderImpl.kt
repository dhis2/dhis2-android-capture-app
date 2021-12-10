package org.dhis2.data.forms.dataentry.fields

import kotlin.reflect.KClass
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.form.ui.provider.LayoutProvider
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType

private val layouts = mapOf<KClass<*>, Int>(
    ScanTextViewModel::class to R.layout.form_scan,
    SectionViewModel::class to R.layout.form_section,
    SpinnerViewModel::class to R.layout.form_option_set_spinner
)

class LayoutProviderImpl : LayoutProvider {

    override fun getLayoutByModel(modelClass: KClass<*>): Int {
        return layouts[modelClass]!!
    }

    override fun getLayoutByType(
        valueType: ValueType?,
        renderingType: ValueTypeRenderingType?,
        sectionRenderingType: ProgramStageSectionRenderingType?
    ): Int {
        return when (valueType) {
            ValueType.AGE -> R.layout.form_age_custom
            ValueType.DATE, ValueType.TIME, ValueType.DATETIME -> R.layout.form_date_time
            ValueType.LONG_TEXT -> R.layout.form_long_text_custom
            ValueType.ORGANISATION_UNIT -> R.layout.form_org_unit
            ValueType.COORDINATE -> R.layout.form_coordinate_custom
            ValueType.IMAGE -> R.layout.form_picture
            ValueType.FILE_RESOURCE,
            ValueType.USERNAME,
            ValueType.TRACKER_ASSOCIATE -> R.layout.form_unsupported
            ValueType.TEXT -> return when (sectionRenderingType) {
                ProgramStageSectionRenderingType.SEQUENTIAL,
                ProgramStageSectionRenderingType.MATRIX -> R.layout.form_option_set_matrix
                else -> when (renderingType) {
                    ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS,
                    ValueTypeRenderingType.VERTICAL_RADIOBUTTONS,
                    ValueTypeRenderingType.HORIZONTAL_CHECKBOXES,
                    ValueTypeRenderingType.VERTICAL_CHECKBOXES -> R.layout.form_option_set_selector
                    else -> R.layout.form_edit_text_custom
                }
            }
            ValueType.TRUE_ONLY,
            ValueType.BOOLEAN -> return when (renderingType) {
                ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS,
                ValueTypeRenderingType.VERTICAL_RADIOBUTTONS,
                ValueTypeRenderingType.DEFAULT -> R.layout.form_radio_button
                ValueTypeRenderingType.TOGGLE -> when (valueType) {
                    ValueType.TRUE_ONLY -> R.layout.form_toggle
                    else -> R.layout.form_radio_button
                }
                ValueTypeRenderingType.HORIZONTAL_CHECKBOXES,
                ValueTypeRenderingType.VERTICAL_CHECKBOXES -> R.layout.form_check_button
                else -> R.layout.form_radio_button
            }
            else -> R.layout.form_edit_text_custom
        }
    }
}
