package org.dhis2.usescases.datasets.dataSetTable

data class DataSetTableModel(
    val dataElement: String?,
    val period: String?,
    val organisationUnit: String?,
    val categoryOptionCombo: String?,
    val attributeOptionCombo: String?,
    val value: String?,
    val storedBy: String?,
    val catOption: String?,
    val listCategoryOption: List<String?>?,
    val catCombo: String?,
) {
    fun setValue(value: String?) = copy(value = value)
}
