package org.dhis2.usescases.datasets.datasetDetail.datasetlist.mapper

import android.content.Context
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel
import org.dhis2.usescases.datasets.datasetDetail.datasetList.mapper.DatasetCardMapper
import org.hisp.dhis.android.core.common.State
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class DatasetCardMapperTest {

    private val context: Context = mock()
    private val resourceManager: ResourceManager = mock()

    private lateinit var mapper: DatasetCardMapper

    @Before
    fun setUp() {
        whenever(context.getString(org.dhis2.R.string.interval_now)) doReturn "now"
        whenever(resourceManager.getString(org.dhis2.R.string.show_more)) doReturn "Show more"
        whenever(resourceManager.getString(org.dhis2.R.string.show_less)) doReturn "Show less"
        whenever(resourceManager.getString(org.dhis2.R.string.completed)) doReturn "Completed"

        mapper = DatasetCardMapper(context, resourceManager)
    }

    @Test
    fun shouldReturnCardFull() {
        val currentDate = Date()

        // Given a dataset without attributes
        val datasetModel = DataSetDetailModel.create(
            "",
            "",
            "",
            "",
            "orgUnitName",
            "nameCatCombo",
            "Dataset PeriodName",
            State.SYNCED,
            "Month",
            true,
            true,
            currentDate,
            "Category option combo name",
        )

        // When dataset is mapped to card item
        val result = mapper.map(
            dataset = datasetModel,
            editable = true,
            onSyncIconClick = {},
            onCardCLick = {},
        )

        // Then all the result is displayed correctly
        assertEquals(result.title, datasetModel.namePeriod())
        assertEquals(result.lastUpdated, currentDate.toDateSpan(context))
        assertEquals(result.additionalInfo[0].value, datasetModel.nameOrgUnit())
        assertEquals(result.additionalInfo[1].value, datasetModel.nameCatCombo())
        assertEquals(
            result.additionalInfo[2].value,
            resourceManager.getString(org.dhis2.R.string.completed),
        )
    }
}
