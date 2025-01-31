package org.dhis2.mobile.aggregates.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@Preview(device = "id:pixel_8_pro", showSystemUi = true, showBackground = true)
@Composable
fun DataSetTableScreenPreview() {
    KoinApplication(
        application = {
            modules(previewModules)
        },
    ) {
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
}

@Preview(device = "id:pixel_c", showSystemUi = true)
@Composable
fun DataSetTableTabletScreenPreview() {
    KoinApplication(
        application = {
            modules(previewModules)
        },
    ) {
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
}

val previewModules = module {
    factory {
        GetDataSetInstanceDetails(
            dataSetUid = "dataSetUid",
            periodId = "periodId",
            orgUnitUid = "orgUnitUid",
            attrOptionComboUid = "attrOptionComboUid",
            dataSetInstanceRepository = object : DataSetInstanceRepository {
                override fun getDataSetInstance(
                    dataSetUid: String,
                    periodId: String,
                    orgUnitUid: String,
                    attrOptionComboUid: String,
                ) = DataSetDetails(
                    titleLabel = "Antenatal Care",
                    dateLabel = "March 2024",
                    orgUnitLabel = "Ngelehum",
                    catOptionComboLabel = "Dhis2",
                )
            },
        )
    }
    viewModel {
        DataSetTableViewModel(get())
    }
}
