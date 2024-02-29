package org.dhis2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.ui.theme.programColorDark
import org.dhis2.ui.theme.programColorLight
import org.dhis2.ui.utils.getAlphaContrastColor

@Composable
fun MetadataIcon(modifier: Modifier = Modifier, metadataIconData: MetadataIconData) {
    when (metadataIconData) {
        is MetadataIconData.Custom -> MetadataResourceCustom(modifier, metadataIconData)
        is MetadataIconData.Resource -> MetadataResourceIcon(modifier, metadataIconData)
    }
}

@Composable
private fun MetadataResourceCustom(
    modifier: Modifier,
    metadataIconData: MetadataIconData.Custom,
) {
    Image(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .size(metadataIconData.sizeInDp.dp),
        bitmap = metadataIconData.file.asImageBitmap(),
        contentDescription = "",
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun MetadataResourceIcon(
    modifier: Modifier,
    metadataIconData: MetadataIconData.Resource,
) {
    Image(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color = Color(metadataIconData.programColor))
            .size(metadataIconData.sizeInDp.dp),
        painter = painterResource(id = metadataIconData.iconResource),
        contentDescription = "",
        colorFilter = ColorFilter.tint(
            metadataIconData.programColor.getAlphaContrastColor(),
        ),
    )
}

@Composable
fun SquareWithNumber(number: Int) {
    Box(
        modifier = Modifier
            .size(25.dp)
            .clip(RoundedCornerShape(4.dp))
            .background("#f2f2f2".toColor()),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+$number",
            color = "#6f6f6f".toColor(),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.rubik_regular)),
        )
    }
}

fun ComposeView.setUpMetadataIcon(
    metadataIconData: MetadataIconData,
    handleDispose: Boolean = true,
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

fun ComposeView.handleComposeDispose() {
    setViewCompositionStrategy(
        ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
    )
}

@Preview
@Composable
fun MetadataIconPreview(
    @PreviewParameter(MetadataIconDataParamProvider::class) metadataIconData: MetadataIconData,
) {
    MetadataIcon(metadataIconData = metadataIconData)
}

fun String.toColor() = Color(android.graphics.Color.parseColor(this))

class MetadataIconDataParamProvider : PreviewParameterProvider<MetadataIconData> {
    override val values: Sequence<MetadataIconData>
        get() = sequenceOf(
            MetadataIconData.Resource(
                programColor = programColorDark.toArgb(),
                iconResource = R.drawable.ic_home_negative,
            ),
            MetadataIconData.Resource(
                programColor = programColorDark.toArgb(),
                iconResource = R.drawable.ic_home_positive,
            ),
            MetadataIconData.Resource(
                programColor = programColorDark.toArgb(),
                iconResource = R.drawable.ic_home_outline,
            ),
            MetadataIconData.Resource(
                programColor = programColorLight.toArgb(),
                iconResource = R.drawable.ic_home_negative,
            ),
            MetadataIconData.Resource(
                programColor = programColorLight.toArgb(),
                iconResource = R.drawable.ic_home_positive,
            ),
            MetadataIconData.Resource(
                programColor = programColorLight.toArgb(),
                iconResource = R.drawable.ic_home_outline,
            ),

        )
}
