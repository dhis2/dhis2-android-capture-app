package org.dhis2.form.ui.provider.inputfield

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.CustomIntentState
import org.hisp.dhis.mobile.ui.designsystem.component.InputCustomIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle

@Composable
fun ProvideCustomIntentInput(
    modifier: Modifier,
    inputStyle: InputStyle = InputStyle.DataInputStyle(),
    fieldUiModel: FieldUiModel,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,
) {
    var values = remember() {
        fieldUiModel.value?.takeIf { it.isNotEmpty() }?.let { mutableStateListOf(it) }
            ?: mutableStateListOf()
    }
    // TODO update intent launcher to use fielduimodel values

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val content = result.data?.data.toString()
                values = mutableStateListOf(content)
                values.add(result.data?.getBooleanExtra("biometricsComplete", false).toString())
                result.data?.getStringExtra("identification")?.let {
                    values.add(it)
                }
                result.data?.getStringExtra("sessionId")?.let {
                    values.add(it)
                }
            }
        }

    val state by remember(values) {
        mutableStateOf(if (values.isNotEmpty()) CustomIntentState.LOADED else CustomIntentState.LAUNCH)
    }
    InputCustomIntent(
        title = "Custom Intent sample",
        buttonText = "launch",
        onLaunch = {
            val intentData = Intent("packageName").apply {
                putExtra("projectId", "projectId")
                putExtra("moduleId", "testDhisModuleId")
                putExtra("userId", "testDhisUserId")
                putExtra("sessionId", "testDhisSessionId")
            }
            val intent = Intent.createChooser(
                intentData,
                "Custom intent!",
            )
            launcher.launch(intent)
        },
        onClear = {},
        customIntentState = state,
        values = values.toList(),
    )
}
