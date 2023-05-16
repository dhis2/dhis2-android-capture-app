package org.dhis2.usescases.table.model

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.dhis2.composetable.model.TableModel

class FakeTableModels(private val context: Context) {

    fun getMultiHeaderTables(
        configFileName: FakeModelType = FakeModelType.MULTIHEADER_TABLE
    ): List<TableModel> {
        val fileInString: String =
            context.assets.open("${configFileName.fileName}.json")
                .bufferedReader().use { it.readText() }

        return Json.decodeFromString(fileInString)
    }
}
