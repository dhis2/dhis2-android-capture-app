package org.dhis2.form.ui.event

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

sealed class RecyclerViewUiEvents {
    data class RequestLocationByMap(
        val uid: String,
        val featureType: FeatureType,
        val value: String?,
    ) : RecyclerViewUiEvents()

    data class ScanQRCode(
        val uid: String,
        val optionSet: String?,
        val renderingType: UiRenderType?,
    ) : RecyclerViewUiEvents()

    data class DisplayQRCode(
        val uid: String,
        val optionSet: String?,
        val value: String,
        val renderingType: UiRenderType?,
        val editable: Boolean,
        val label: String,
    ) : RecyclerViewUiEvents()

    data class OpenOrgUnitDialog(
        val uid: String,
        val label: String,
        val value: String?,
        val orgUnitSelectorScope: OrgUnitSelectorScope?,
    ) : RecyclerViewUiEvents()

    data class OpenFile(
        val field: FieldUiModel,
    ) : RecyclerViewUiEvents()

    data class OpenChooserIntent(
        val action: String,
        val value: String?,
        val uid: String,
    ) : RecyclerViewUiEvents()

    data class LaunchCustomIntent(
        val customIntent: CustomIntentModel?,
        val uid: String,
    ) : RecyclerViewUiEvents()

    class SelectPeriod(
        val uid: String,
        val title: String,
        val periodType: PeriodType,
        val minDate: Date?,
        val maxDate: Date?,
    ) : RecyclerViewUiEvents()
}
