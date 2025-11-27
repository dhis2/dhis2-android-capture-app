package org.dhis2.maps.location

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.getTextStyle

const val LOCATION_TIME_LEFT = 30

@Composable
fun AccuracyIndicator(
    modifier: Modifier = Modifier,
    accuracyIndicatorState: AccuracyIndicatorState = rememberAccuracyIndicatorState(timeLeft = LOCATION_TIME_LEFT),
    accuracyRange: AccuracyRange,
    minLocationPrecision: Int? = null,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    LaunchedEffect(key1 = accuracyRange) {
        accuracyIndicatorState.updateAccuracy(this, accuracyRange)
    }

    Layout(
        modifier = modifier,
        content = {
            Box(
                modifier =
                    Modifier
                        .size(24.dp),
            ) {
                if (accuracyIndicatorState.shouldDisplayProgress(accuracyRange)) {
                    ProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        progress = accuracyIndicatorState.accuracyProgress(),
                        type = ProgressIndicatorType.CIRCULAR_SMALL,
                    )
                } else {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_gps_fixed),
                        contentDescription = "location set",
                        tint = SurfaceColor.Primary,
                    )
                }
            }

            Text(
                modifier =
                    Modifier
                        .height(24.dp)
                        .wrapContentHeight(Alignment.CenterVertically),
                text = buildAccuracyText(accuracyRange),
            )
            Tag(
                label = accuracyRangeLabel(accuracyRange),
                type = accuracyTagType(accuracyRange),
            )
            Text(
                text =
                    messageText(
                        accuracyRange = accuracyRange,
                        timeLeft = accuracyIndicatorState.timeLeft,
                        minLocationPrecision = minLocationPrecision,
                    ),
                style =
                    getTextStyle(style = DHIS2TextStyle.BODY_SMALL).copy(
                        color = TextColor.OnSurfaceLight,
                    ),
            )
        },
        measurePolicy = { measurables, constraints ->

            val placeables =
                measurables.map { measurable ->
                    measurable.measure(constraints)
                }

            val totalHeight =
                placeables[0].height + (
                    placeables.getOrNull(3)?.let { messagePlaceable ->
                        messagePlaceable.height + with(density) { 4.dp.toPx() }.toInt()
                    } ?: 0
                )

            layout(constraints.maxWidth, totalHeight) {
                val noLocation =
                    (accuracyIndicatorState.timeLeft == 0) and (accuracyRange is AccuracyRange.None)

                val displayMessage = accuracyIndicatorState.displayMessage(accuracyRange)

                if (!noLocation) {
                    placeProgressIndicator(
                        placeables[0],
                        constraints.maxWidth,
                        totalHeight,
                        accuracyIndicatorState.progressPosition,
                    )
                } else {
                    placeNoLocationMessage(
                        placeables[3],
                        constraints.maxWidth,
                        totalHeight,
                    )
                }
                if (accuracyIndicatorState.displayInfo(accuracyRange)) {
                    accuracyIndicatorState.updateVerticalOffset(
                        scope,
                        ((totalHeight - placeables[0].height) / 2)
                            .takeIf { !displayMessage } ?: 0,
                    )

                    placeAccuracyRange(
                        placeables[1],
                        placeables[0].width,
                        with(density) { 8.dp.toPx() }.toInt(),
                        accuracyIndicatorState.verticalOffset,
                    )
                    placeAccuracyLabel(
                        placeables[2],
                        placeables[0].width,
                        placeables[1].width,
                        accuracyIndicatorState.verticalOffset + with(density) { 2.dp.toPx() }.toInt(),
                        with(density) { 24.dp.toPx() }.toInt(),
                    )
                    if (displayMessage) {
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

@Composable
private fun buildAccuracyText(accuracyRange: AccuracyRange) =
    buildAnnotatedString {
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
    }

@Composable
private fun accuracyRangeLabel(accuracyRange: AccuracyRange) =
    when (accuracyRange) {
        is AccuracyRange.None -> ""
        is AccuracyRange.Good -> stringResource(id = R.string.accuracy_good)
        is AccuracyRange.Low -> stringResource(id = R.string.accuracy_low)
        is AccuracyRange.Medium -> stringResource(id = R.string.accuracy_medium)
        is AccuracyRange.VeryGood -> stringResource(id = R.string.accuracy_verygood)
    }

private fun accuracyTagType(accuracyRange: AccuracyRange) =
    when (accuracyRange) {
        is AccuracyRange.Good -> TagType.SUCCESS
        is AccuracyRange.Low -> TagType.ERROR
        is AccuracyRange.Medium -> TagType.WARNING
        is AccuracyRange.None -> TagType.DEFAULT
        is AccuracyRange.VeryGood -> TagType.SUCCESS
    }

@Composable
private fun messageText(
    accuracyRange: AccuracyRange,
    timeLeft: Int,
    minLocationPrecision: Int?,
) = when {
    timeLeft == 0 && accuracyRange is AccuracyRange.None ->
        stringResource(id = R.string.location_not_available)

    timeLeft == 0 && accuracyRange !is AccuracyRange.None ->
        stringResource(id = R.string.accuracy_can_not_improve) + (
            minLocationPrecision?.let {
                if (accuracyRange.value <= it) {
                    " ${stringResource(id = R.string.accuracy_minimun_set_allow, it)}"
                } else {
                    " ${stringResource(id = R.string.accuracy_minimun_set_not_allow, it)}"
                }
            } ?: ""
        )

    else ->
        stringResource(id = R.string.accuracy_please_wait)
}

private fun Placeable.PlacementScope.placeProgressIndicator(
    progressIndicatorPlaceable: Placeable,
    totalWidth: Int,
    totalHeight: Int,
    progressPosition: Float,
) {
    val centerPosition = (totalWidth - progressIndicatorPlaceable.width) / 2
    val centerPositionY = (totalHeight - progressIndicatorPlaceable.height) / 2
    progressIndicatorPlaceable.placeRelative(
        (progressPosition * centerPosition).toInt(),
        centerPositionY,
    )
}

private fun Placeable.PlacementScope.placeAccuracyRange(
    accuracyRangePlaceable: Placeable,
    indicatorWidth: Int,
    offset: Int,
    verticalOffset: Int,
) {
    accuracyRangePlaceable.placeRelative(indicatorWidth + offset, verticalOffset)
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

private fun Placeable.PlacementScope.placeNoLocationMessage(
    messagePlaceable: Placeable,
    totalWidth: Int,
    totalHeight: Int,
) {
    val centerPosition = (totalWidth - messagePlaceable.width) / 2
    val centerPositionY = (totalHeight - messagePlaceable.height) / 2
    messagePlaceable.placeRelative(
        centerPosition,
        centerPositionY,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AccuracyIndicatorPreview(
    @PreviewParameter(AccuracyRangeParameterProvider::class) accuracyRange: AccuracyRange,
) {
    AccuracyIndicator(accuracyRange = accuracyRange)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AccuracyIndicatorNoneWithTimeLeftPreview(
    @PreviewParameter(TimeLeftParameterProvider::class) timeLeft: Int,
) {
    AccuracyIndicator(
        accuracyRange = AccuracyRange.None(),
        accuracyIndicatorState =
            rememberAccuracyIndicatorState(
                timeLeft = timeLeft,
            ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AccuracyIndicatorGoodWithTimeLeftPreview(
    @PreviewParameter(TimeLeftParameterProvider::class) timeLeft: Int,
) {
    AccuracyIndicator(
        accuracyRange = AccuracyRange.None(),
        accuracyIndicatorState =
            rememberAccuracyIndicatorState(
                timeLeft = timeLeft,
            ),
    )
}

internal class AccuracyRangeParameterProvider : PreviewParameterProvider<AccuracyRange> {
    override val values =
        sequenceOf(
            AccuracyRange.None(),
            AccuracyRange.Low(210),
            AccuracyRange.Medium(35),
            AccuracyRange.Good(20),
            AccuracyRange.VeryGood(5),
        )
}

internal class TimeLeftParameterProvider : PreviewParameterProvider<Int> {
    override val values =
        sequenceOf(
            10,
            0,
        )
}

@Preview(showBackground = true)
@Composable
private fun NoneToValuePreview() {
    var accuracyRange: AccuracyRange by remember {
        mutableStateOf(AccuracyRange.None())
    }
    DHIS2Theme {
        Column(
            Modifier
                .fillMaxWidth(),
            verticalArrangement = spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.height(64.dp), Alignment.Center) {
                AccuracyIndicator(
                    accuracyRange = accuracyRange,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            Button(text = "Change accuracy") {
                accuracyRange =
                    when (accuracyRange) {
                        is AccuracyRange.None ->
                            AccuracyRange.Low(210)

                        is AccuracyRange.Good ->
                            AccuracyRange.VeryGood(5)

                        is AccuracyRange.Low ->
                            AccuracyRange.Medium(35)

                        is AccuracyRange.Medium ->
                            AccuracyRange.Good(20)

                        is AccuracyRange.VeryGood ->
                            AccuracyRange.None()
                    }
            }
        }
    }
}
