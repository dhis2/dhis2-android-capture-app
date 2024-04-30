package org.dhis2.usescases.datasets.dataSetTable

data class DataSetRenderDetails(
    val dataSetDisplayName: String,
    val organisationUnitDisplayName: String,
    val periodDisplayLabel: String,
    val attrOptionComboDisplayName: String,
    val isComplete: Boolean,
) {
    fun title() = dataSetDisplayName
    fun subtitle(): String {
        val subtitle: StringBuilder = StringBuilder(periodDisplayLabel)
            .append(" | ")
            .append(organisationUnitDisplayName)
        if (attrOptionComboDisplayName != "default") {
            subtitle.append(" | ")
                .append(attrOptionComboDisplayName)
        }
        return subtitle.toString()
    }
}
