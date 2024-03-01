package org.dhis2.form.ui.provider.inputfield

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputSequential
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.internal.ImageCardData

@Composable
internal fun ProvideSequentialInput(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    context: Context,
    intentHandler: (FormIntent) -> Unit,
) {
    val inputCardDataList = rememberInputCardList(
        options = fieldUiModel.optionSetConfiguration?.optionsToDisplay(),
        optionMetadataIconMap = fieldUiModel.optionSetConfiguration?.optionMetadataIcon,
    )
    var matrixSelectedItem by rememberSelectedOption(
        fieldUiModel = fieldUiModel,
        inputCardDataList = inputCardDataList,
    )

    InputSequential(
        title = fieldUiModel.label,
        data = inputCardDataList,
        state = fieldUiModel.inputState(),
        selectedData = matrixSelectedItem,
        onSelectionChanged = { newSelectedItem ->
            matrixSelectedItem = if (matrixSelectedItem?.uid == newSelectedItem.uid) {
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
        inputStyle = inputStyle,
        painterFor = inputCardDataList.filterIsInstance<ImageCardData.CustomIconData>().associate {
            it.uid to BitmapPainter(it.image)
        },
    )
}
