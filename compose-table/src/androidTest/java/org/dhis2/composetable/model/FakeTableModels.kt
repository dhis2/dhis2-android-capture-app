package org.dhis2.composetable.model

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class FakeTableModels(private val context: Context) {

    fun getMultiHeaderTables(
        configFileName:FakeModelType = FakeModelType.MULTIHEADER_TABLE
    ): List<TableModel> {
        val fileInString: String =
            InstrumentationRegistry.getInstrumentation()
                .context.assets.open("${configFileName.fileName}.json")
                .bufferedReader().use { it.readText() }

        return Json.decodeFromString(fileInString)
    }
}
