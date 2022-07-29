package org.dhis2.composetable.model

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class FakeTableModels(private val context: Context) {

    fun getMultiHeaderTables(): List<TableModel> {
        val fileInString: String =
            context.assets.open("multi_header_table_list.json").bufferedReader().use { it.readText() }

        return Json.decodeFromString(fileInString)
    }
}