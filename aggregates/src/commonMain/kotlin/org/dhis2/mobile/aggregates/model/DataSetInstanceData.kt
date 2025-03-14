package org.dhis2.mobile.aggregates.model

internal data class DataSetInstanceData(
    val dataSetDetails: DataSetDetails,
    val dataSetRenderingConfig: DataSetRenderingConfig,
    val dataSetSections: List<DataSetSection>,
    val initialSectionToLoad: Int,
)
