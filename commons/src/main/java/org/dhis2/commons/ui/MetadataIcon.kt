package org.dhis2.commons.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.commons.R
import org.dhis2.commons.resources.ColorUtils

@Composable
fun MetadataIcon(
    modifier: Modifier = Modifier,
    metadataIconData: MetadataIconData
) {
    Image(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color = Color(metadataIconData.programColor))
            .size(metadataIconData.sizeInDp.dp),
        painter = painterResource(id = metadataIconData.iconResource),
        contentDescription = "",
        colorFilter = ColorFilter.tint(
            Color(ColorUtils.getAlphaContrastColor(metadataIconData.programColor))
        )
    )
}

fun ComposeView.setUpMetadataIcon(
    metadataIconData: MetadataIconData,
    handleDispose: Boolean = true
) {
    if (handleDispose) {
        handleComposeDispose()
    }
    setContent {
        MdcTheme {
            MetadataIcon(metadataIconData = metadataIconData)
        }
    }
}

fun ComposeView.handleComposeDispose(){
    setViewCompositionStrategy(
        ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
    )
}

@Preview
@Composable
fun MetadataIconPreview(
    @PreviewParameter(MetadataIconDataParamProvider::class) metadataIconData: MetadataIconData
) {
    MetadataIcon(metadataIconData = metadataIconData)
}

class MetadataIconDataParamProvider : PreviewParameterProvider<MetadataIconData> {
    override val values: Sequence<MetadataIconData>
        get() = sequenceOf(
            MetadataIconData(
                programColor = android.graphics.Color.parseColor("#00BCD4"),
                iconResource = R.drawable.ic_home_negative
            ),
            MetadataIconData(
                programColor = android.graphics.Color.parseColor("#00BCD4"),
                iconResource = R.drawable.ic_home_positive
            ),
            MetadataIconData(
                programColor = android.graphics.Color.parseColor("#00BCD4"),
                iconResource = R.drawable.ic_home_outline
            ),
            MetadataIconData(
                programColor = android.graphics.Color.parseColor("#84FFFF"),
                iconResource = R.drawable.ic_home_negative
            ),
            MetadataIconData(
                programColor = android.graphics.Color.parseColor("#84FFFF"),
                iconResource = R.drawable.ic_home_positive
            ),
            MetadataIconData(
                programColor = android.graphics.Color.parseColor("#84FFFF"),
                iconResource = R.drawable.ic_home_outline
            )

        )
}