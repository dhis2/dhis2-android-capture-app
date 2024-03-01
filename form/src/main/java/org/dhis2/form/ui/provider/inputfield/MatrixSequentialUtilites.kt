package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import org.dhis2.form.model.FieldUiModel
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.mobile.ui.designsystem.component.internal.ImageCardData

@Composable
fun rememberInputCardList(
    options: List<Option>?,
    optionMetadataIconMap: Map<String, MetadataIconData>?,
) = remember(options) {
    options?.map { option ->
        val metadataIconData =
            optionMetadataIconMap?.get(option.uid()) ?: throw IllegalArgumentException()

        var icon = option.style().icon() ?: "dhis2_dhis2_logo_positive"
        if (!icon.startsWith("dhis2_")) {
            icon = "dhis2_$icon"
        }

        val imageCardItem = when (metadataIconData) {
            is MetadataIconData.Custom ->
                ImageCardData.CustomIconData(
                    uid = option.code() ?: "",
                    label = option.displayName() ?: "",
                    image = metadataIconData.file.asImageBitmap(),
                )

            is MetadataIconData.Resource ->
                ImageCardData.IconCardData(
                    uid = option.code() ?: "",
                    label = option.displayName() ?: "",
                    iconRes = icon,
                    iconTint = Color(metadataIconData.programColor),
                )
        }
        imageCardItem
    } ?: emptyList()
}

@Composable
fun rememberSelectedOption(fieldUiModel: FieldUiModel, inputCardDataList: List<ImageCardData>) =
    remember(fieldUiModel.displayName) {
        mutableStateOf(
            inputCardDataList.find { it.uid == fieldUiModel.displayName || it.label == fieldUiModel.displayName },
        )
    }
