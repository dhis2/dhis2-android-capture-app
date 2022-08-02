package org.dhis2.composetable.model

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

const val MULTI_HEADER_TABLE = "multi_header_table_list.json"

class FakeTableModels(private val context: Context) {

    fun getMultiHeaderTables(): List<TableModel> {
        val fileInString: String =
            context.assets.open(MULTI_HEADER_TABLE).bufferedReader().use { it.readText() }

        return Json.decodeFromString(fileInString)
    }
}
