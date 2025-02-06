package org.dhis2.mobile.aggregates.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.domain.GetDataSetRenderingConfig
import org.dhis2.mobile.aggregates.domain.GetDataSetSections
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
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
            modules(previewModules(true))
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

@Preview(device = "id:pixel_c", showSystemUi = true, showBackground = true)
@Composable
fun DataSetTableTabletScreenPreview() {
    KoinApplication(
        application = {
            modules(previewModules(true))
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

fun previewModules(useVerticalTabs: Boolean) = module {
    single<DataSetInstanceRepository> {
        object : DataSetInstanceRepository {
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

            override fun getDataSetInstanceSections(dataSetUid: String): List<DataSetSection> =
                listOf(
                    DataSetSection(
                        uid = "uid1",
                        title = "Section 1",
                    ),
                    DataSetSection(
                        uid = "uid2",
                        title = "Section 2",
                    ),
                    DataSetSection(
                        uid = "uid3",
                        title = "Section 3",
                    ),
                )

            override fun getRenderingConfig(dataSetUid: String) =
                DataSetRenderingConfig(useVerticalTabs)
        }
    }

    factory {
        GetDataSetInstanceDetails(
            dataSetUid = "dataSetUid",
            periodId = "periodId",
            orgUnitUid = "orgUnitUid",
            attrOptionComboUid = "attrOptionComboUid",
            dataSetInstanceRepository = get(),
        )
    }
    factory {
        GetDataSetSections(
            "dataSetUid",
            get(),
        )
    }

    factory {
        GetDataSetRenderingConfig(
            "dataSetUid",
            get(),
        )
    }

    viewModel {
        DataSetTableViewModel(get(), get(), get())
    }
}
