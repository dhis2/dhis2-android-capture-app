package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.dhis2.composetable.model.TableModel

class FakeTableModels(private val context: Context) {

    /**
     * Tables with different value types:
     * age
     * coordinate
     * date
     * Email
     * File
     * Image
     * Letter
     * LongText
     */
    fun getTestDataSetSectionA(): List<TableModel> =
        Json.decodeFromString(parseJsonToString("ds_test_dataset_section_a.json"))

    /**
     * Tables with different value types:
     * Negative integer
     * Org unit
     * Phone number
     * Positive integer
     * Positive or zero integer
     * Text
     * Option set
     * Time
     * Unit interval
     * Url
     * Username
     * YesNo
     * YesOnly
     */
    fun getTestDataSetSectionB(): List<TableModel> =
        Json.decodeFromString(parseJsonToString("ds_test_dataset_section_b.json"))

    private fun parseJsonToString(jsonString: String): String {
        return context.assets.open(jsonString)
            .bufferedReader()
            .use { it.readText() }
    }
}