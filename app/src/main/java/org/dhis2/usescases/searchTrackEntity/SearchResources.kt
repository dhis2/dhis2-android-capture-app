package org.dhis2.usescases.searchTrackEntity

import android.content.Context
import org.dhis2.R

class SearchResources(val context: Context) {

    fun searchCriteriaNotMet(typeName: String) =
        context.getString(R.string.search_criteria_not_met).format(typeName)

    fun searchMinNumAttributes(minNumber: Int) =
        context.getString(R.string.search_min_num_attr).format(minNumber)

    fun searchInit() = context.getString(R.string.search_init)

    fun searchMaxTeiReached(maxNumber: Int) =
        context.getString(R.string.search_max_tei_reached).format(maxNumber)

    fun teiTypeHasNoAttributes(typeName: String) =
        context.getString(R.string.tei_type_has_no_attributes).format(typeName)
}
