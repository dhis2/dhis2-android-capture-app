package org.dhis2.commons.resources

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.ContextCompat
import org.dhis2.commons.R
import org.dhis2.commons.network.NetworkUtils
import org.hisp.dhis.android.core.D2Manager

class ResourceManager(
    val context: Context,
    private val colorUtils: ColorUtils,
) {
    fun getString(
        @StringRes stringResource: Int,
    ) = getWrapperContext().getString(stringResource)

    fun getString(
        @StringRes stringResource: Int,
        vararg arguments: String,
    ) = getWrapperContext().getString(stringResource).format(*arguments)

    fun getString(
        @StringRes stringResource: Int,
        quantity: Int,
    ) = getWrapperContext().getString(stringResource).format(quantity)

    fun getPlural(
        @PluralsRes pluralResource: Int,
        quantity: Int,
    ) = getWrapperContext().resources.getQuantityString(pluralResource, quantity)

    fun getPlural(
        @PluralsRes pluralResource: Int,
        quantity: Int,
        vararg arguments: Any,
    ) = getWrapperContext().resources.getQuantityString(pluralResource, quantity, *arguments)

    fun formatWithEnrollmentLabel(
        programUid: String?,
        @StringRes stringResource: Int,
        quantity: Int,
        formatWithQuantity: Boolean = false,
    ): String {
        val enrollmentLabel =
            defaultEnrollmentLabel(programUid, getString(stringResource).startsWith("%s"), quantity)

        return if (formatWithQuantity) {
            getString(stringResource).format(quantity, enrollmentLabel)
        } else {
            getString(stringResource).format(enrollmentLabel)
        }
    }

    fun defaultEnrollmentLabel(
        programUid: String?,
        capitalize: Boolean = false,
        quantity: Int = 1,
    ): String {
        val enrollmentLabel =
            try {
                D2Manager
                    .getD2()
                    .programModule()
                    .programs()
                    .uid(programUid)
                    .blockingGet()
                    ?.displayEnrollmentLabel()
            } catch (e: Exception) {
                null
            } ?: getPlural(R.plurals.enrollment, quantity)

        return if (capitalize) {
            enrollmentLabel.capitalize(Locale.current)
        } else {
            enrollmentLabel
        }
    }

    fun getObjectStyleDrawableResource(
        icon: String?,
        @DrawableRes defaultResource: Int,
    ): Int =
        icon?.let {
            val iconName = if (icon.startsWith("ic_")) icon else "ic_$icon"
            val iconResource =
                getWrapperContext().resources.getIdentifier(
                    iconName,
                    "drawable",
                    getWrapperContext().packageName,
                )
            if (iconResource != 0 && iconResource != -1 && drawableExists(iconResource)) {
                iconResource
            } else {
                R.drawable.ic_default_icon
            }
        } ?: defaultResource

    private fun drawableExists(iconResource: Int): Boolean =
        try {
            ContextCompat.getDrawable(getWrapperContext(), iconResource)
            true
        } catch (e: Exception) {
            false
        }

    fun getColorFrom(hexColor: String?): Int =
        hexColor?.let {
            colorUtils.parseColor(it)
        } ?: -1

    fun parseD2Error(throwable: Throwable) =
        D2ErrorUtils(getWrapperContext(), NetworkUtils(getWrapperContext()))
            .getErrorMessage(throwable)

    fun defaultEventLabel(): String = getWrapperContext().getString(R.string.events)

    fun defaultDataSetLabel(): String = getWrapperContext().getString(R.string.data_sets)

    fun defaultTeiLabel(): String = getWrapperContext().getString(R.string.tei)

    fun sectionFeedback(): String = getWrapperContext().getString(R.string.section_feedback)

    fun sectionIndicators(): String = getWrapperContext().getString(R.string.section_indicators)

    fun sectionCharts(): String = getWrapperContext().getString(R.string.section_charts)

    fun sectionChartsAndIndicators(): String = getWrapperContext().getString(R.string.section_charts_indicators)

    fun defaultIndicatorLabel(): String = getWrapperContext().getString(R.string.info)

    fun getWrapperContext() =
        try {
            LocaleSelector(context, D2Manager.getD2()).updateUiLanguage()
        } catch (_: Exception) {
            context
        }
}
