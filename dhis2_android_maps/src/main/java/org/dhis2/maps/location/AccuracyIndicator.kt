package org.dhis2.maps.location

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import org.dhis2.maps.R
import org.dhis2.maps.model.AccuracyRange
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.Tag
import org.hisp.dhis.mobile.ui.designsystem.component.TagType
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2TextStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.getTextStyle

@Composable
fun AccuracyIndicator(
    accuracyRange: AccuracyRange,
) {
    val density = LocalDensity.current
    val accuracyIndicatorState = rememberAccuracyIndicatorState()

    LaunchedEffect(key1 = accuracyRange) {
        accuracyIndicatorState.updateAccuracy(this, accuracyRange)
    }

    Layout(
        modifier = Modifier,
        content = {
            ProgressIndicator(
                modifier = Modifier
                    .size(24.dp),
                type = ProgressIndicatorType.CIRCULAR,
            )
            Text(
                modifier = Modifier.height(24.dp),
                text = buildAnnotatedString {
                    val accuracyRangeLabel = stringResource(id = R.string.accuracy_range) + ": "
                    val accuracyValueLabel = "${accuracyRange.value}m"

                    addStyle(
                        getTextStyle(style = DHIS2TextStyle.BODY_LARGE).toSpanStyle().copy(
                            color = TextColor.OnSurfaceLight,
                        ),
                        0,
                        accuracyRangeLabel.length,
                    )
                    append(accuracyRangeLabel)
                    addStyle(
                        getTextStyle(style = DHIS2TextStyle.BODY_LARGE).toSpanStyle().copy(
                            color = TextColor.OnSurface,
                        ),
                        accuracyRangeLabel.length,
                        accuracyRangeLabel.length + accuracyValueLabel.length,
                    )
                    append(accuracyValueLabel)
                },
            )
            Tag(
                label = when (accuracyRange) {
                    is AccuracyRange.None -> ""
                    is AccuracyRange.Good -> stringResource(id = R.string.accuracy_good)
                    is AccuracyRange.Low -> stringResource(id = R.string.accuracy_low)
                    is AccuracyRange.Medium -> stringResource(id = R.string.accuracy_medium)
                    is AccuracyRange.VeryGood -> stringResource(id = R.string.accuracy_verygood)
                },
                type = TagType.DEFAULT,
            )
            Text(
                text = stringResource(id = R.string.accuracy_please_wait),
                style = getTextStyle(style = DHIS2TextStyle.BODY_SMALL).copy(
                    color = TextColor.OnSurfaceLight,
                ),
            )
        },
        measurePolicy = { measurables, constraints ->

            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }

            val totalHeight = when {
                accuracyRange !is AccuracyRange.None -> {
                    placeables[0].height + (
                        placeables.getOrNull(3)?.let { messagePlaceable ->
                            messagePlaceable.height + with(density) { 4.dp.toPx() }.toInt()
                        } ?: 0
                        )
                }

                else -> placeables[0].height
            }

            val itemsTotalWidth = placeables.sumOf { it.width }
            val layoutTotalWidth =
                if (itemsTotalWidth > constraints.maxWidth) constraints.maxWidth else itemsTotalWidth

            layout(layoutTotalWidth, totalHeight) {
                placeProgressIndicator(
                    placeables[0],
                    layoutTotalWidth,
                    accuracyIndicatorState.progressPosition,
                )
                if (accuracyIndicatorState.displayInfo(accuracyRange)) {
                    placeAccuracyRange(
                        placeables[1],
                        placeables[0].width,
                        with(density) { 8.dp.toPx() }.toInt(),
                    )
                    placeAccuracyLabel(
                        placeables[2],
                        placeables[0].width,
                        placeables[1].width,
                        with(density) { 2.dp.toPx() }.toInt(),
                        with(density) { 24.dp.toPx() }.toInt(),
                    )
                    if ((accuracyRange is AccuracyRange.Low) or (accuracyRange is AccuracyRange.Medium)) {
                        placeMessage(
                            placeables[3],
                            placeables[0].width,
                            placeables[0].height,
                            with(density) { 4.dp.toPx() }.toInt(),
                            with(density) { 8.dp.toPx() }.toInt(),
                        )
                    }
                }
            }
        },
    )
}

private fun Placeable.PlacementScope.placeProgressIndicator(
    progressIndicatorPlaceable: Placeable,
    totalWidth: Int,
    progressPosition: Float,
) {
    val centerPosition = (totalWidth - progressIndicatorPlaceable.width) / 2
    progressIndicatorPlaceable.placeRelative((progressPosition * centerPosition).toInt(), 0)
}

private fun Placeable.PlacementScope.placeAccuracyRange(
    accuracyRangePlaceable: Placeable,
    indicatorWidth: Int,
    offset: Int,
) {
    accuracyRangePlaceable.placeRelative(indicatorWidth + offset, 0)
}

private fun Placeable.PlacementScope.placeAccuracyLabel(
    accuracyLabelPlaceable: Placeable,
    indicatorWidth: Int,
    accuracyRangeWidth: Int,
    verticalOffset: Int,
    horizontalOffset: Int,
) {
    accuracyLabelPlaceable.placeRelative(
        indicatorWidth + accuracyRangeWidth + horizontalOffset,
        verticalOffset,
    )
}

private fun Placeable.PlacementScope.placeMessage(
    messagePlaceable: Placeable,
    indicatorWidth: Int,
    indicatorHeight: Int,
    verticalOffset: Int,
    horizontalOffset: Int,
) {
    messagePlaceable.placeRelative(
        indicatorWidth + horizontalOffset,
        indicatorHeight + verticalOffset,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AccuracyIndicatorPreview(
    @PreviewParameter(AccuracyRangeParameterProvider::class) accuracyRange: AccuracyRange,
) {
    AccuracyIndicator(accuracyRange = accuracyRange)
}

internal class AccuracyRangeParameterProvider : PreviewParameterProvider<AccuracyRange> {
    override val values = sequenceOf(
        AccuracyRange.None(),
        AccuracyRange.Low(210),
        AccuracyRange.Medium(35),
        AccuracyRange.Good(20),
        AccuracyRange.VeryGood(5),
    )
}

@Preview(showBackground = true)
@Composable
private fun NoneToValuePreview() {
    var accuracyRange: AccuracyRange by remember {
        mutableStateOf(AccuracyRange.None())
    }
    Column(
        Modifier
            .fillMaxWidth(),
        verticalArrangement = spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AccuracyIndicator(accuracyRange = accuracyRange)
        Button(text = "Change accuracy") {
            accuracyRange = when {
                accuracyRange is AccuracyRange.None ->
                    AccuracyRange.Low(210)

                else ->
                    AccuracyRange.None()
            }
        }
    }
}
