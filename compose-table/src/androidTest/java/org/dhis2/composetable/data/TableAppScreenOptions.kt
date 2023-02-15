package org.dhis2.composetable.data

data class TableAppScreenOptions(
    val inputRowsOptions: List<InputRowOption> = emptyList()
) {
    fun requiresTextInput(tableId: String, row: Int): Boolean {
        return inputRowsOptions.find {
            it.tableId == tableId && it.row == row
        }?.requireInput ?: true
    }
}

data class InputRowOption(
    val tableId: String,
    val row: Int,
    val requireInput: Boolean
)