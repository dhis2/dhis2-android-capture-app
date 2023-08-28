package org.dhis2.usescases.datasets.dataSetTable

import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSection

data class DataSetScreenState(
    val sections: List<DataSetSection>,
    val renderDetails: DataSetRenderDetails?,
    val initialSectionToOpenUid: String?,
) {
    fun firstSectionUid() = sections.firstOrNull()?.uid
}
