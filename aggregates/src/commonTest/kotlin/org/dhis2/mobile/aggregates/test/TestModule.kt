package org.dhis2.mobile.aggregates.test

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.data.ValueParser
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.domain.GetDataSetRenderingConfig
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSections
import org.dhis2.mobile.aggregates.model.CellElement
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceConfiguration
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionConfiguration
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionData
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun testModule(
    testingDispatcher: TestDispatcher,
    useVerticalTabs: Boolean,
) = module {
    factory<ValueParser> {
        object : ValueParser {
            override fun parseValue(uid: String, value: String): String {
                return value
            }
        }
    }

    factory<Dispatcher> {
        StandardTestDispatcher()
        Dispatcher(
            main = { testingDispatcher },
            io = { testingDispatcher },
            default = { testingDispatcher },
        )
    }

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
                        uid = "section_uid1",
                        title = "Section 1",
                    ),
                    DataSetSection(
                        uid = "section_uid2",
                        title = "Section 2",
                    ),
                    DataSetSection(
                        uid = "section_uid3",
                        title = "Section 3",
                    ),
                )

            override fun getRenderingConfig(dataSetUid: String) =
                DataSetRenderingConfig(useVerticalTabs)

            override fun dataSetInstanceConfiguration(
                dataSetUid: String,
                periodId: String,
                orgUnitUid: String,
                attrOptionComboUid: String,
                sectionUid: String,
            ) = DataSetInstanceConfiguration(
                hasDataElementDecoration = true,
                compulsoryDataElements = emptyList(),
                allDataSetElements = listOf(
                    CellElement(
                        "uid1",
                        "catCombo1",
                        "Row 1",
                        null,
                        false,
                    ),
                ),
                greyedOutFields = emptyList(),
                editable = true,
            )

            override fun getDataSetInstanceSectionCells(
                dataSetElements: List<CellElement>,
                dataSetUid: String,
                sectionUid: String,
            ): List<DataSetInstanceSectionData> = listOf(
                DataSetInstanceSectionData(
                    uid = "catCombo1",
                    label = "CatCombo 1",
                    subgroups = listOf(
                        "Cat 1",
                        "Cat 2",
                    ),
                    cellElements = listOf(
                        CellElement(
                            "uid1",
                            "catCombo1",
                            "Row 1",
                            null,
                            false,
                        ),
                    ),
                ),
            )

            override fun getTableGroupHeaders(categoryUids: List<String>): List<List<String>> =
                listOf(
                    listOf("Cat Option 1.1", "Cat Option 1.2"),
                    listOf("Cat Option 2.1", "Cat Option 2.2"),
                )

            override fun dataSetInstanceSectionConfiguration(sectionUid: String) =
                DataSetInstanceSectionConfiguration(
                    showRowTotals = true,
                )

            override fun conflicts(
                dataSetUid: String,
                periodId: String,
                orgUnitUid: String,
                attrOptionComboUid: String,
                dataElementUid: String,
                categoryOptionComboUid: String,
            ): Pair<List<String>, List<String>> = Pair(
                emptyList(),
                emptyList(),
            )

            override fun cellValue(
                periodId: String,
                orgUnitUid: String,
                dataElementUid: String,
                categoryOptionComboUid: String,
                attrOptionComboUid: String,
            ): String? = null

            override fun categoryOptionCombinations(categoryUids: List<String>): List<String> =
                listOf(
                    "catOptionCombo1",
                    "catOptionCombo2",
                    "catOptionCombo3",
                    "catOptionCombo4",
                )
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

    factory {
        GetDataSetSectionData(
            "dataSetUid",
            "orgUnitUid",
            "periodId",
            "attrOptionComboUid",
            get(),
            get(),
        )
    }

    viewModel {
        DataSetTableViewModel(get(), get(), get(), get(), get())
    }
}
