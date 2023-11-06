package org.dhis2.form.ui.provider.inputfield

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.dhis2.commons.resources.ObjectStyleUtils
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputMatrix
import org.hisp.dhis.mobile.ui.designsystem.component.internal.IconCardData

@Composable
internal fun ProvideMatrixInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    context: Context,
    intentHandler: (FormIntent) -> Unit,
) {
    val inputCardDataList = remember {
        mutableListOf<IconCardData>()
    }
    var matrixSelectedItem by remember(fieldUiModel) { mutableStateOf<IconCardData?>(null) }

    fieldUiModel.optionSetConfiguration?.optionsToDisplay()?.forEach() { option ->
        val color =
            ObjectStyleUtils.getColorResource(context, option.style().color(), R.color.colorPrimary)
        var icon = option.style().icon() ?: "dhis2_dhis2_logo_positive"
        if (!icon.startsWith("dhis2_")) {
            icon = "dhis2_$icon"
        }
        val iconCardItem = IconCardData(
            uid = option.code() ?: "",
            label = option.displayName() ?: "",
            iconRes = icon,
            iconTint = Color(color),
        )
        if (!inputCardDataList.contains(iconCardItem)) {
            inputCardDataList.add(
                iconCardItem,
            )
        }
        if (fieldUiModel.displayName == option.code() || fieldUiModel.displayName == option.displayName()) matrixSelectedItem = iconCardItem
    }

    InputMatrix(
        title = fieldUiModel.label,
        data = inputCardDataList,
        state = fieldUiModel.inputState(),
        selectedData = matrixSelectedItem,
        onSelectionChanged = { newSelectedItem ->
            matrixSelectedItem = if (matrixSelectedItem == newSelectedItem) {
                null
            } else {
                newSelectedItem
            }
            fieldUiModel.onItemClick()
            val valueToSave = if (matrixSelectedItem == null) null else matrixSelectedItem?.uid
            intentHandler(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    valueToSave,
                    fieldUiModel.valueType,
                ),
            )
        },
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        modifier = modifier,
    )
}
