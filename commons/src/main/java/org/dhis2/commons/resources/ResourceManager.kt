package org.dhis2.commons.resources

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import org.dhis2.commons.R
import org.hisp.dhis.android.core.D2Manager

class ResourceManager(val context: Context) {

    fun getString(@StringRes stringResource: Int) = getWrapperContext().getString(stringResource)

    fun getObjectStyleDrawableResource(icon: String?, @DrawableRes defaultResource: Int): Int {
        return icon?.let {
            val iconName = if (icon.startsWith("ic_")) icon else "ic_$icon"
            val iconResource =
                getWrapperContext().resources.getIdentifier(
                    iconName,
                    "drawable",
                    getWrapperContext().packageName
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
            ColorUtils.parseColor(it)
        } ?: -1
    }

    fun getColorOrDefaultFrom(hexColor: String?): Int {
        return ColorUtils.getColorFrom(
            hexColor,
            ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY_LIGHT)
        )
    }

    fun parseD2Error(throwable: Throwable) =
        D2ErrorUtils(getWrapperContext()).getErrorMessage(throwable)

    fun defaultEventLabel(): String = getWrapperContext().getString(R.string.events)
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
