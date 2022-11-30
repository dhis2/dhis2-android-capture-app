package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

const val NO_SECTION = "NO_SECTION"
data class DataSetSection(val uid: String, val name: String?) {
    fun title() = name ?: uid
}

fun List<DataSetSection>.replaceNoSection(noSectionLabel: String): List<DataSetSection> {
    return this.map { section ->
        if (section.uid == NO_SECTION) {
            section.copy(name = noSectionLabel)
        } else {
            section
        }
    }
}
