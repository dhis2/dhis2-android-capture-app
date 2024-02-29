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

    fun getString(@StringRes stringResource: Int) = getWrapperContext().getString(stringResource)

    fun getString(@StringRes stringResource: Int, vararg arguments: String) =
        getWrapperContext().getString(stringResource).format(*arguments)

    fun getString(@StringRes stringResource: Int, quantity: Int) =
        getWrapperContext().getString(stringResource).format(quantity)

    fun getPlural(@PluralsRes pluralResource: Int, quantity: Int) =
        getWrapperContext().resources.getQuantityString(pluralResource, quantity)

    fun getPlural(@PluralsRes pluralResource: Int, quantity: Int, vararg arguments: Any) =
        getWrapperContext().resources.getQuantityString(pluralResource, quantity, *arguments)

    fun formatWithEnrollmentLabel(
        programUid: String?,
        @StringRes stringResource: Int,
        quantity: Int,
        formatWithQuantity: Boolean = false,
    ): String {
        val enrollmentLabel = try {
            D2Manager.getD2().programModule().programs().uid(programUid).blockingGet()
                ?.enrollmentLabel()
        } catch (e: Exception) {
            null
        } ?: getPlural(R.plurals.enrollment, quantity)

        return with(getString(stringResource)) {
            val finalLabel = if (this@with.startsWith("%s")) {
                enrollmentLabel.capitalize(Locale.current)
            } else {
                enrollmentLabel
            }

            if (formatWithQuantity) {
                format(quantity, finalLabel)
            } else {
                format(finalLabel)
            }
        }
    }

    fun getObjectStyleDrawableResource(icon: String?, @DrawableRes defaultResource: Int): Int {
        return icon?.let {
            val iconName = if (icon.startsWith("ic_")) icon else "ic_$icon"
            val iconResource =
                getWrapperContext().resources.getIdentifier(
                    iconName,
                    "drawable",
                    getWrapperContext().packageName,
                )
            if (iconResource != 0 && iconResource != -1 && drawableExists(iconResource)
            ) {
                iconResource
            } else {
                R.drawable.ic_default_icon
            }
        } ?: defaultResource
    }

    private fun drawableExists(iconResource: Int): Boolean {
        return try {
            ContextCompat.getDrawable(getWrapperContext(), iconResource)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getColorFrom(hexColor: String?): Int {
        return hexColor?.let {
            colorUtils.parseColor(it)
        } ?: -1
    }

    fun getColorOrDefaultFrom(hexColor: String?): Int {
        return colorUtils.getColorFrom(
            hexColor,
            colorUtils.getPrimaryColor(context, ColorType.PRIMARY_LIGHT),
        )
    }

    fun parseD2Error(throwable: Throwable) =
        D2ErrorUtils(getWrapperContext(), NetworkUtils(getWrapperContext()))
            .getErrorMessage(throwable)

    fun defaultEventLabel(): String = getWrapperContext().getString(R.string.events)
    fun getEventLabel(
        programStageUid: String? = null,
        quantity: Int = 1,
    ) = try {
        D2Manager.getD2().programModule()
            .programStages().uid(programStageUid)
            .blockingGet()?.eventLabel()
    } catch (e: Exception) {
        null
    } ?: getPlural(R.plurals.event_label, quantity)

    fun formatWithEventLabel(
        @StringRes stringResource: Int,
        programStageUid: String? = null,
        quantity: Int = 1,
        formatWithQuantity: Boolean = false,
    ): String {
        val eventLabel = getEventLabel(programStageUid, quantity)
        return with(getString(stringResource)) {
            val finalLabel = if (this@with.startsWith("%s")) {
                eventLabel.capitalize(Locale.current)
            } else {
                eventLabel
            }
            if (formatWithQuantity) {
                format(quantity, finalLabel)
            } else {
                format(finalLabel)
            }
        }
    }

    fun defaultDataSetLabel(): String = getWrapperContext().getString(R.string.data_sets)
    fun defaultTeiLabel(): String = getWrapperContext().getString(R.string.tei)
    fun jiraIssueSentMessage(): String = getWrapperContext().getString(R.string.jira_issue_sent)
    fun jiraIssueSentErrorMessage(): String =
        getWrapperContext().getString(R.string.jira_issue_sent_error)

    fun sectionFeedback(): String = getWrapperContext().getString(R.string.section_feedback)
    fun sectionIndicators(): String = getWrapperContext().getString(R.string.section_indicators)
    fun sectionCharts(): String = getWrapperContext().getString(R.string.section_charts)
    fun sectionChartsAndIndicators(): String =
        getWrapperContext().getString(R.string.section_charts_indicators)

    fun defaultIndicatorLabel(): String = getWrapperContext().getString(R.string.info)

    fun getWrapperContext() = try {
        LocaleSelector(context, D2Manager.getD2()).updateUiLanguage()
    } catch (exception: Exception) {
        context
    }

    fun defaultTableLabel(): String = context.getString(R.string.default_table_header_label)
    fun defaultEmptyDataSetSectionLabel(): String =
        context.getString(R.string.default_empty_dataset_section_label)
}
