package org.dhis2.mobile.aggregates.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@Preview(device = "id:pixel_8a")
@Composable
fun DataSetTableScreenPreview() {
    DHIS2Theme {
        DataSetInstanceScreen(
            parameters = DataSetInstanceParameters(
                "dataSetUid",
                "periodId",
                "orgUnitUid",
                "attrOptionComboUid",
            ),
            false,
        ) {}
    }
}

@Preview(device = "id:pixel_c")
@Composable
fun DataSetTableTabletScreenPreview() {
    DHIS2Theme {
        DataSetInstanceScreen(
            parameters = DataSetInstanceParameters(
                "dataSetUid",
                "periodId",
                "orgUnitUid",
                "attrOptionComboUid",
            ),
            true,
        ) {}
    }
}
