package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.ImageCardData

fun imageCardDataWithUidAndLabel(
    imageCardData: ImageCardData,
    optionCode: String,
    label: String,
): ImageCardData =
    when (imageCardData) {
        is ImageCardData.CustomIconData -> imageCardData.copy(uid = optionCode, label = label)
        is ImageCardData.IconCardData -> imageCardData.copy(uid = optionCode, label = label)
    }

@Composable
fun rememberSelectedOption(
    fieldUiModel: FieldUiModel,
    inputCardDataList: List<ImageCardData>,
) = remember(inputCardDataList, fieldUiModel.displayName) {
    mutableStateOf(
        inputCardDataList.find { it.uid == fieldUiModel.displayName || it.label == fieldUiModel.displayName },
    )
}
