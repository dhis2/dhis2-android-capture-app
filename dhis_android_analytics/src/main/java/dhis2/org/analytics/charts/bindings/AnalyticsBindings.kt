package dhis2.org.analytics.charts.bindings

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeDrawable.BOTTOM_END
import com.google.android.material.badge.BadgeUtils
import dhis2.org.analytics.charts.ui.ChartModel
import org.dhis2.commons.resources.ColorUtils
import org.hisp.dhis.android.core.period.PeriodType

@BindingAdapter("filter_visualization")
fun ImageView.setFilterVisualization(chartModel: ChartModel) {
    val currentFilters = chartModel.currentFilters()
    if (currentFilters == 0) {
        visibility = View.GONE
    } else {
        visibility = View.VISIBLE
        val badge = BadgeDrawable.create(context).apply {
            number = currentFilters
            badgeGravity = BOTTOM_END
            backgroundColor = ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)
        }
        BadgeUtils.attachBadgeDrawable(badge, this)
    }
}

fun PeriodType.datePattern(): String = when (this) {
    PeriodType.Daily,
    PeriodType.Weekly,
    PeriodType.WeeklySaturday,
    PeriodType.WeeklySunday,
    PeriodType.WeeklyThursday,
    PeriodType.WeeklyWednesday,
    PeriodType.BiWeekly,
    PeriodType.Monthly,
    PeriodType.BiMonthly,
    PeriodType.Quarterly,
    PeriodType.SixMonthly,
    PeriodType.SixMonthlyApril,
    PeriodType.SixMonthlyNov -> {
        "MMM YYYY"
    }

    PeriodType.Yearly,
    PeriodType.FinancialApril,
    PeriodType.FinancialJuly,
    PeriodType.FinancialOct,
    PeriodType.FinancialNov -> {
        "YYYY"
    }
}
