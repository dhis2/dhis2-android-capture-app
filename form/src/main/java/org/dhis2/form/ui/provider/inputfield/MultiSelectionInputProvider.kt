package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.InputMultiSelection

@Composable
internal fun ProvideMultiSelectionInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    val dataMap =
        buildMap {
            fieldUiModel.optionSetConfiguration?.optionFlow?.collectAsLazyPagingItems()?.let { paging ->
                repeat(paging.itemCount) { index ->
                    val optionData = paging[index]
                    put(
                        optionData?.option?.code() ?: "",
                        CheckBoxData(
                            uid = optionData?.option?.uid() ?: "",
                            checked = optionData?.option?.code()?.let { fieldUiModel.value?.split(",")?.contains(it) } ?: false,
                            enabled = true,
                            textInput = optionData?.option?.displayName() ?: "",
                        ),
                    )
                }
            }
        }

    val (codeList, data) = dataMap.toList().unzip()

    InputMultiSelection(
        modifier = modifier,
        title = fieldUiModel.label,
        items = data,
        state = fieldUiModel.inputState(),
        supportingTextData = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        onItemsSelected = {
            val checkedValues =
                it.mapNotNull { checkBoxData ->
                    if (checkBoxData.checked) {
                        val selectedIndex = data.indexOfFirst { originalData -> originalData.uid == checkBoxData.uid }
                        codeList[selectedIndex]
                    } else {
                        null
                    }
                }

            intentHandler(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    checkedValues.joinToString(separator = ","),
                    fieldUiModel.valueType,
                ),
            )
        },
        onClearItemSelection = {
            intentHandler(FormIntent.ClearValue(fieldUiModel.uid))
        },
    )
}
