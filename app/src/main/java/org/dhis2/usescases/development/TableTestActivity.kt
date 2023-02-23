package org.dhis2.usescases.development

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.material.composethemeadapter.MdcTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.model.KeyboardInputType
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.composetable.ui.DataSetTableScreen

class TableTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme {
                val fakeTableModel = getMultiHeaderTables(FakeModelType.MANDATORY_TABLE)
                val screenState: MutableStateFlow<TableScreenState> = MutableStateFlow(
                    TableScreenState(
                        tables = fakeTableModel,
                        selectNext = false
                    )
                )

                val collectedState by screenState.collectAsState()

                DataSetTableScreen(
                    tableScreenState = collectedState,
                    onCellClick = { _, cell, _ ->
                        TextInputModel(
                            id = cell.id ?: "",
                            mainLabel = "-",
                            secondaryLabels = emptyList(),
                            currentValue = cell.value,
                            keyboardInputType = KeyboardInputType.TextInput(),
                            error = null
                        )
                    },
                    onEdition = { },
                    onSaveValue = { _, _ -> }
                )
            }
        }
    }

    fun getMultiHeaderTables(
        configFileName: FakeModelType = FakeModelType.MULTIHEADER_TABLE
    ): List<TableModel> {
        val fileInString: String =
            this.assets.open("${configFileName.fileName}.json")
                .bufferedReader().use { it.readText() }

        return Json.decodeFromString(fileInString)
    }

    enum class FakeModelType(val fileName: String) {
        MULTIHEADER_TABLE("multi_header_table_list"),
        MANDATORY_TABLE("mandatory_cell_table_list")
    }
}
