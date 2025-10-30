package org.dhis2.mobile.aggregates.data.mappers

import org.dhis2.mobile.aggregates.model.DataSetCustomTitle
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetEdition
import org.dhis2.mobile.aggregates.model.TextAlignment
import org.hisp.dhis.android.core.dataset.CustomText
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.dataset.TextAlign

internal fun DataSetInstance.toDataSetDetails(
    isDefaultCatCombo: Boolean,
    customText: CustomText?,
    periodLabel: String,
    isCompleted: Boolean,
    edition: DataSetEdition,
) = DataSetDetails(
    customTitle =
        customText?.toCustomTitle() ?: DataSetCustomTitle(
            header = null,
            subHeader = null,
            textAlignment = null,
            isConfiguredTitle = false,
        ),
    dataSetTitle = this.dataSetDisplayName(),
    dateLabel = periodLabel,
    orgUnitLabel = this.organisationUnitDisplayName(),
    catOptionComboLabel =
        if (isDefaultCatCombo) {
            null
        } else {
            this.attributeOptionComboDisplayName()
        },
    isCompleted = isCompleted,
    edition = edition,
)

internal fun CustomText?.toCustomTitle() =
    DataSetCustomTitle(
        header = this?.header(),
        subHeader = this?.subHeader(),
        textAlignment = this?.align()?.toTextAlignment() ?: TextAlignment.LEFT,
        isConfiguredTitle = this?.header() != null,
    )

internal fun TextAlign.toTextAlignment() =
    when (this) {
        TextAlign.LINE_START -> TextAlignment.LEFT
        TextAlign.CENTER -> TextAlignment.CENTER
        TextAlign.LINE_END -> TextAlignment.RIGHT
    }
