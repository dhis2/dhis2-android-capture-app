package org.dhis2.android.rtsm.ui.home.model

import java.time.LocalDateTime
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.TransactionType.CORRECTION
import org.dhis2.android.rtsm.data.TransactionType.DISCARD
import org.dhis2.android.rtsm.data.TransactionType.DISTRIBUTION
import org.dhis2.android.rtsm.utils.UIText
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

data class SettingsUiState(
    val programUid: String,
    val transactionType: TransactionType = DISTRIBUTION,
    val facility: OrganisationUnit? = null,
    val destination: Option? = null,
    val transactionDate: LocalDateTime = LocalDateTime.now()
) {
    fun hasFacilitySelected() = facility != null
    fun hasDestinationSelected() = destination != null
    fun fromFacilitiesLabel(): UIText = facility?.let {
        val orgUnitName = it.displayName().toString()
        return when (transactionType) {
            DISTRIBUTION -> {
                UIText.StringRes(R.string.subtitle, orgUnitName)
            }
            DISCARD -> {
                UIText.StringRes(R.string.subtitle, orgUnitName)
            }
            CORRECTION -> {
                UIText.StringRes(R.string.subtitle, orgUnitName)
            }
        }
    } ?: UIText.StringRes(R.string.from_facility)

    fun deliverToLabel(): UIText? = when (transactionType) {
        DISTRIBUTION -> destination?.let {
            UIText.StringRes(R.string.subtitle, it.displayName().toString())
        } ?: UIText.StringRes(R.string.to_facility)
        else -> null
    }

    fun facilityName() = facility?.let {
        it.displayName().toString()
    } ?: ""
}
