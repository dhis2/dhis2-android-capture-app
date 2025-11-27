package org.dhis2.mobile.aggregates.di.mappers

import android.content.Intent
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_ATTRIBUTE_OPTION_COMBO_UID
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_DATA_SET_UID
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_ORGANISATION_UNIT_UID
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_PERIOD_ID
import org.dhis2.mobile.aggregates.ui.constants.OPEN_ERROR_LOCATION

fun Intent.toDataSetInstanceParameters() =
    DataSetInstanceParameters(
        dataSetUid = requireNotNull(getStringExtra(INTENT_EXTRA_DATA_SET_UID)),
        periodId = requireNotNull(getStringExtra(INTENT_EXTRA_PERIOD_ID)),
        organisationUnitUid = requireNotNull(getStringExtra(INTENT_EXTRA_ORGANISATION_UNIT_UID)),
        attributeOptionComboUid = requireNotNull(getStringExtra(INTENT_EXTRA_ATTRIBUTE_OPTION_COMBO_UID)),
        openErrorLocation = getBooleanExtra(OPEN_ERROR_LOCATION, false),
    )
