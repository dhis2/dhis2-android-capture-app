package org.dhis2.form.ui

import androidx.annotation.LayoutRes
import kotlin.reflect.KClass
import org.dhis2.form.R
import org.dhis2.form.ui.provider.LayoutProvider
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.program.SectionRenderingType

private val layouts = mapOf<KClass<*>, Int>()

class LayoutProviderImpl : LayoutProvider {

    override fun getLayoutByModel(modelClass: KClass<*>): Int {
        return layouts[modelClass]!!
    }

    override fun getLayoutByType(
        valueType: ValueType?,
        renderingType: ValueTypeRenderingType?,
        optionSet: String?,
        sectionRenderingType: SectionRenderingType?
    ): Int {
        return when (valueType) {
            ValueType.AGE ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_age_custom
                )
            ValueType.DATE, ValueType.TIME, ValueType.DATETIME ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_date_time
                )
            ValueType.LONG_TEXT ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_long_text_custom
                )
            ValueType.ORGANISATION_UNIT ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_org_unit
                )
            ValueType.COORDINATE ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_coordinate_custom
                )
            ValueType.IMAGE ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_picture
                )
            ValueType.TRUE_ONLY,
            ValueType.BOOLEAN -> return when (renderingType) {
                ValueTypeRenderingType.TOGGLE -> when (valueType) {
                    ValueType.TRUE_ONLY ->
                        getLayoutForOptionSet(
                            optionSet,
                            sectionRenderingType,
                            renderingType,
                            R.layout.form_toggle
                        )
                    else ->
                        getLayoutForOptionSet(
                            optionSet,
                            sectionRenderingType,
                            renderingType,
                            R.layout.form_radio_button
                        )
                }
                ValueTypeRenderingType.HORIZONTAL_CHECKBOXES,
                ValueTypeRenderingType.VERTICAL_CHECKBOXES ->
                    getLayoutForOptionSet(
                        optionSet,
                        sectionRenderingType,
                        renderingType,
                        R.layout.form_check_button
                    )
                else ->
                    getLayoutForOptionSet(
                        optionSet,
                        sectionRenderingType,
                        renderingType,
                        R.layout.form_radio_button
                    )
            }
            ValueType.LETTER ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_letter
                )
            ValueType.PHONE_NUMBER ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_phone_number
                )
            ValueType.EMAIL ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_email
                )
            ValueType.NUMBER ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_number
                )
            ValueType.UNIT_INTERVAL ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_unit_interval
                )
            ValueType.PERCENTAGE ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_percentage
                )
            ValueType.INTEGER ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_integer
                )
            ValueType.INTEGER_POSITIVE ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_integer_positive
                )
            ValueType.INTEGER_NEGATIVE ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_integer_negative
                )
            ValueType.INTEGER_ZERO_OR_POSITIVE ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_integer_zero_positive
                )
            ValueType.URL ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_url
                )
            ValueType.FILE_RESOURCE -> R.layout.form_unsupported
            ValueType.REFERENCE,
            ValueType.GEOJSON,
            ValueType.USERNAME,
            ValueType.TRACKER_ASSOCIATE -> R.layout.form_unsupported
            else ->
                getLayoutForOptionSet(
                    optionSet,
                    sectionRenderingType,
                    renderingType,
                    R.layout.form_edit_text_custom
                )
        }
    }

    private fun getLayoutForOptionSet(
        optionSet: String?,
        sectionRenderingType: SectionRenderingType?,
        renderingType: ValueTypeRenderingType?,
        @LayoutRes defaultLayout: Int
    ): Int {
        return when {
            shouldRenderAsMatrixImage(optionSet, sectionRenderingType, renderingType) ->
                R.layout.form_option_set_matrix
            shouldRenderAsSelector(optionSet, renderingType) ->
                R.layout.form_option_set_selector
            shouldRenderAsSpinner(optionSet) ->
                R.layout.form_option_set_spinner
            shouldRenderAsScan(renderingType) ->
                R.layout.form_scan
            else ->
                defaultLayout
        }
    }

    private fun shouldRenderAsScan(renderingType: ValueTypeRenderingType?): Boolean {
        return when (renderingType) {
            ValueTypeRenderingType.QR_CODE, ValueTypeRenderingType.BAR_CODE -> true
            else -> false
        }
    }

    private fun shouldRenderAsSpinner(optionSet: String?): Boolean {
        return optionSet != null
    }

    private fun shouldRenderAsSelector(
        optionSet: String?,
        renderingType: ValueTypeRenderingType?
    ): Boolean {
        val isOptionSet = optionSet != null
        val isSelectorRendering = when (renderingType) {
            ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS,
            ValueTypeRenderingType.VERTICAL_RADIOBUTTONS,
            ValueTypeRenderingType.HORIZONTAL_CHECKBOXES,
            ValueTypeRenderingType.VERTICAL_CHECKBOXES -> {
                true
            }
            else -> false
        }
        return isOptionSet && isSelectorRendering
    }

    private fun shouldRenderAsMatrixImage(
        optionSet: String?,
        sectionRenderingType: SectionRenderingType?,
        renderingType: ValueTypeRenderingType?
    ): Boolean {
        val isOptionSet = optionSet != null
        val isDefaultRendering =
            renderingType == null || renderingType == ValueTypeRenderingType.DEFAULT
        val isSectionRenderingMatrix =
            sectionRenderingType ?: SectionRenderingType.LISTING != SectionRenderingType.LISTING
        return isOptionSet && isDefaultRendering && isSectionRenderingMatrix
    }

    override fun getLayoutForSection() = R.layout.form_section
}
