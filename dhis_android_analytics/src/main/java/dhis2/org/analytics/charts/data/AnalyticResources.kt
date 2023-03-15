package dhis2.org.analytics.charts.data

import android.content.Context
import dhis2.org.R
import org.hisp.dhis.android.core.analytics.AnalyticsException

class AnalyticResources(val context: Context) {
    fun analyticsExceptionMessage(analyticsException: AnalyticsException): String {
        return when (analyticsException) {
            is AnalyticsException.InvalidArguments ->
                context.getString(R.string.error_invalid_arguments)
            is AnalyticsException.InvalidCategory ->
                context.getString(R.string.error_invalid_category).format(analyticsException.uid)
            is AnalyticsException.InvalidCategoryOption ->
                context.getString(R.string.error_invalid_category_option)
                    .format(analyticsException.uid)
            is AnalyticsException.InvalidDataElement ->
                context.getString(R.string.error_invalid_data_element)
                    .format(analyticsException.uid)
            is AnalyticsException.InvalidDataElementOperand ->
                context.getString(R.string.error_invalid_data_element_operand)
                    .format(analyticsException.uid)
            is AnalyticsException.InvalidIndicator ->
                context.getString(R.string.error_invalid_indicator).format(analyticsException.uid)
            is AnalyticsException.InvalidOrganisationUnit ->
                context.getString(R.string.error_invalid_org_unit).format(analyticsException.uid)
            is AnalyticsException.InvalidOrganisationUnitGroup ->
                context.getString(R.string.error_invalid_org_unit_group)
                    .format(analyticsException.uid)
            is AnalyticsException.InvalidOrganisationUnitLevel ->
                context.getString(R.string.error_invalid_org_unit_level)
                    .format(analyticsException.id)
            is AnalyticsException.InvalidProgramIndicator ->
                context.getString(R.string.error_invalid_program_indicator)
                    .format(analyticsException.uid)
            is AnalyticsException.InvalidVisualization ->
                context.getString(R.string.error_invalid_visualization)
                    .format(analyticsException.uid)
            is AnalyticsException.ParserException ->
                context.getString(R.string.error_parsing_visualization)
            is AnalyticsException.SQLException ->
                context.getString(R.string.error_database)
            is AnalyticsException.InvalidProgram ->
                context.getString(R.string.error_invalid_program)
                    .format(analyticsException.uid)
            is AnalyticsException.InvalidTrackedEntityAttribute ->
                context.getString(R.string.error_invalid_attribute)
                    .format(analyticsException.uid)
            is AnalyticsException.UnsupportedAggregationType ->
                context.getString(R.string.error_unsupported_aggregation_type)
                    .format(analyticsException.aggregationType.name)
            else -> ""
        }
    }
}
