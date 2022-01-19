package org.dhis2.form.ui

import kotlin.reflect.KClass
import org.dhis2.form.R
import org.dhis2.form.ui.provider.LayoutProvider
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType

private val layouts = mapOf<KClass<*>, Int>()

class LayoutProviderImpl : LayoutProvider {

    override fun getLayoutByModel(modelClass: KClass<*>): Int {
        return layouts[modelClass]!!
    }

    override fun getLayoutByType(
        valueType: ValueType?,
        renderingType: ValueTypeRenderingType?,
        optionSet: String?,
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
            ValueType.TEXT ->
                return if (!optionSet.isNullOrEmpty()) {
                    when (sectionRenderingType) {
                        ProgramStageSectionRenderingType.SEQUENTIAL,
                        ProgramStageSectionRenderingType.MATRIX ->
                            R.layout.form_option_set_matrix
                        else -> when (renderingType) {
                            ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS,
                            ValueTypeRenderingType.VERTICAL_RADIOBUTTONS,
                            ValueTypeRenderingType.HORIZONTAL_CHECKBOXES,
                            ValueTypeRenderingType.VERTICAL_CHECKBOXES ->
                                R.layout.form_option_set_selector
                            ValueTypeRenderingType.QR_CODE,
                            ValueTypeRenderingType.BAR_CODE ->
                                R.layout.form_scan
                            else -> R.layout.form_option_set_spinner
                        }
                    }
                } else {
                    R.layout.form_edit_text_custom
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

    override fun getLayoutForSection() = R.layout.form_section
}
