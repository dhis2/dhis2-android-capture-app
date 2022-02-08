package org.dhis2.commons.resources

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import org.dhis2.commons.R
import org.dhis2.commons.filters.FilterResources

class ResourceManager(val context: Context) {

    val filterResources by lazy { FilterResources(context) }

    fun getString(@StringRes stringResource: Int) = context.getString(stringResource)

    fun getObjectStyleDrawableResource(icon: String?, @DrawableRes defaultResource: Int): Int {
        return icon?.let {
            val iconName = if (icon.startsWith("ic_")) icon else "ic_$icon"
            var iconResource =
                context.resources.getIdentifier(iconName, "drawable", context.packageName)
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
            ContextCompat.getDrawable(context, iconResource)
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

    fun parseD2Error(throwable: Throwable) = D2ErrorUtils(context).getErrorMessage(throwable)

    fun defaultEventLabel(): String = context.getString(R.string.events)
    fun defaultDataSetLabel(): String = context.getString(R.string.data_sets)
    fun defaultTeiLabel(): String = context.getString(R.string.tei)
    fun jiraIssueSentMessage(): String = context.getString(R.string.jira_issue_sent)
    fun jiraIssueSentErrorMessage(): String = context.getString(R.string.jira_issue_sent_error)
    fun sectionFeedback(): String = context.getString(R.string.section_feedback)
    fun sectionIndicators(): String = context.getString(R.string.section_indicators)
    fun sectionCharts(): String = context.getString(R.string.section_charts)
    fun sectionChartsAndIndicators(): String = context.getString(R.string.section_charts_indicators)
    fun defaultIndicatorLabel(): String = context.getString(R.string.info)
}
