package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.model.CellElement

internal object DisableDataElementGrouping {
    operator fun invoke(dataSetElementsInSection: List<CellElement>): List<List<CellElement>> {
        val result = mutableListOf<List<CellElement>>()
        var currentGroup: String? = null
        var currentList = mutableListOf<CellElement>()

        for (element in dataSetElementsInSection) {
            if (element.categoryComboUid == currentGroup) {
                // If the current element belongs to the same group, add it to the current list
                currentList.add(element)
            } else {
                // If the group changes, save the current list (if not empty) and start a new one
                if (currentList.isNotEmpty()) {
                    result.add(currentList)
                }
                currentGroup = element.categoryComboUid
                currentList = mutableListOf(element)
            }
        }

        // Add the last group if it exists
        if (currentList.isNotEmpty()) {
            result.add(currentList)
        }

        return result
    }
}
