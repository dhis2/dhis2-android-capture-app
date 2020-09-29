package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import java.util.Date
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage

data class EventViewModel(
    val type: EventViewModelType,
    val stage: ProgramStage?,
    val event: Event?,
    val eventCount: Int,
    val lastUpdate: Date?,
    val isSelected: Boolean,
    val canAddNewEvent: Boolean,
    val orgUnitName: String,
    val catComboName: String?,
    val dataElementValues: List<Pair<String, String?>>?,
    val groupedByStage: Boolean? = false,
    var valueListIsOpen: Boolean = false,
    val showTopShadow: Boolean = false,
    val showBottomShadow: Boolean = false
) {
    fun toggleValueList() {
        this.valueListIsOpen = !valueListIsOpen
    }

    fun valuesSpannableString(): SpannableStringBuilder {
        val stringBuilder = SpannableStringBuilder()
        dataElementValues?.forEach { nameValuePair ->
            if (nameValuePair.second != "-") {
                val value = SpannableString(nameValuePair.second)
                val colorToUse = when {
                    dataElementValues.indexOf(nameValuePair) % 2 == 0 -> {
                        Color.parseColor("#8A333333")
                    }
                    else -> Color.parseColor("#61333333")
                }
                value.setSpan(
                    ForegroundColorSpan(colorToUse),
                    0,
                    value.length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                stringBuilder.append(value)
                if (dataElementValues.indexOf(nameValuePair) != dataElementValues.size - 1) {
                    stringBuilder.append(" ")
                }
            }
        }
        return stringBuilder
    }
}
