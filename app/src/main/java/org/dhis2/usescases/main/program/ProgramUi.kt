package org.dhis2.usescases.main.program

import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.commons.bindings.addIf
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.ui.icons.toIconData
import org.dhis2.data.service.SyncStatusData
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.toColor
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyleData
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ExpandableItemColumn
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBarData
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatarSize
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.VerticalInfoListCard
import org.hisp.dhis.mobile.ui.designsystem.component.internal.ImageCardData
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import java.util.Date

enum class ProgramLayout {
    DEFAULT, MEDIUM, LARGE;

    fun metadataAvatarSize() = when (this) {
        DEFAULT -> MetadataAvatarSize.S()
        MEDIUM -> MetadataAvatarSize.L()
        LARGE -> MetadataAvatarSize.XL()
    }
}

@Composable
fun ProgramList(
    programs: List<ProgramUiModel>?,
    onItemClick: (programUiModel: ProgramUiModel) -> Unit,
    onGranularSyncClick: (programUiModel: ProgramUiModel) -> Unit,
    downLoadState: SyncStatusData?,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DownloadMessage(downLoadState)

        programs?.let {
            if (programs.isEmpty()) {
                NoAccessMessage()
            }

            ExpandableItemColumn(
                modifier = Modifier
                    .fillMaxSize(),
                itemList = programs,
            ) { program, verticalPadding, onSizeChanged ->

                ProgramItem(
                    modifier = Modifier,
                    program = program,
                    programLayout = getProgramLayout(programs),
                    verticalPadding = verticalPadding,
                    onSizeChanged = { size ->
                        if (!program.isDownloading()) {
                            onSizeChanged(size)
                        }
                    },
                    onItemClick = onItemClick,
                    onGranularSyncClick = onGranularSyncClick,
                )
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ProgressIndicator(type = ProgressIndicatorType.CIRCULAR_SMALL)
            }
        }
    }
}

private fun getProgramLayout(programs: List<ProgramUiModel>) = when {
    programs.size < 3 -> ProgramLayout.LARGE
    programs.size < 4 -> ProgramLayout.MEDIUM
    else -> ProgramLayout.DEFAULT
}

@Composable
private fun DownloadMessage(downLoadState: SyncStatusData?) {
    val visibility = when {
        downLoadState?.running == false ->
            timeVisibility()

        else -> downLoadState?.canDisplayMessage() == true
    }

    AnimatedVisibility(
        visible = visibility,
        enter = expandIn(
            expandFrom = Alignment.Center,
            animationSpec = tween(
                easing = {
                    OvershootInterpolator().getInterpolation(it)
                },
            ),
        ),
        exit = shrinkOut(shrinkTowards = Alignment.Center),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(
                    top = Spacing.Spacing16,
                    bottom = Spacing.Spacing8,
                    start = Spacing.Spacing16,
                    end = Spacing.Spacing16,
                ),
        ) {
            InfoBar(
                infoBarData = InfoBarData(
                    text = downloadInfoText(downLoadState),
                    icon = {
                        Icon(
                            imageVector = when {
                                downLoadState?.running == false ->
                                    Icons.Outlined.Celebration

                                else -> Icons.Outlined.Info
                            },
                            contentDescription = "error",
                            tint = when {
                                downLoadState?.running == false ->
                                    SurfaceColor.CustomGreen

                                else -> TextColor.OnSurfaceLight
                            },
                        )
                    },
                    color = when {
                        downLoadState?.running == false ->
                            SurfaceColor.CustomGreen

                        else -> TextColor.OnSurfaceLight
                    },
                    backgroundColor = SurfaceColor.Surface,
                ),
            )
        }
    }
}

@Composable
private fun downloadInfoText(downLoadState: SyncStatusData?) = when {
    downLoadState?.running == false -> stringResource(R.string.successful_sync)
    downLoadState?.downloadingEvents == true -> stringResource(
        id = R.string.syncing_something,
        stringResource(id = R.string.events).lowercase(),
    )

    downLoadState?.downloadingTracker == true -> stringResource(
        id = R.string.syncing_something,
        stringResource(id = R.string.programs).lowercase(),
    )

    downLoadState?.downloadingDataSetValues == true -> stringResource(
        id = R.string.syncing_something,
        stringResource(id = R.string.data_sets).lowercase(),
    )

    downLoadState?.downloadingMedia == true -> stringResource(
        id = R.string.syncing_something,
        stringResource(id = R.string.file_resources).lowercase(),
    )

    else -> ""
}

@Composable
fun StateIcon(
    state: State,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    if (state != State.RELATIONSHIP && state != State.SYNCED) {
        IconButton(onClick = onClick, enabled = enabled) {
            val (iconResource, tintColor) = state.toIconData()
            Icon(
                imageVector = iconResource,
                tint = tintColor,
                contentDescription = "sync",
            )
        }
    }
}

@Composable
fun DownloadingProgress() {
    CircularProgressIndicator(
        modifier = Modifier
            .size(24.dp)
            .padding(2.dp),
        color = Color(
            ColorUtils().getPrimaryColor(LocalContext.current, ColorType.PRIMARY),
        ),
        strokeWidth = 2.dp,
    )
}

@Composable
fun DownloadedIcon() {
    Icon(
        painter = painterResource(id = R.drawable.ic_download_done),
        contentDescription = "downloaded",
        tint = Color.Unspecified,
    )
}

@Composable
fun DownloadErrorIcon() {
    Icon(
        modifier = Modifier,
        painter = painterResource(id = R.drawable.ic_download_error),
        contentDescription = "download error",
        tint = Color.Unspecified,
    )
}

@Composable
fun timeVisibility(hideAfterMillis: Long = 3000): Boolean {
    var visible by remember { mutableStateOf(true) }
    DisposableEffect(Unit) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = {
            visible = false
        }
        handler.postDelayed(runnable, hideAfterMillis)
        onDispose { handler.removeCallbacks(runnable) }
    }
    return visible
}

@Composable
@Preview
fun DownloadMedia() {
    Box(
        modifier = Modifier.padding(vertical = 16.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            elevation = 13.dp,
            backgroundColor = Color.White,
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_perm_media),
                    contentDescription = "",
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.downloading_image_resources),
                    style = LocalTextStyle.current.copy(
                        color = Color.Black.copy(alpha = 0.87f),
                        fontFamily = FontFamily(Font(R.font.rubik_regular)),
                    ),
                )
                DownloadingProgress()
            }
        }
    }
}

@Composable
@Preview
fun NoAccessMessage() {
    InfoBar(
        modifier = Modifier.padding(Spacing.Spacing16),
        infoBarData = InfoBarData(
            text = stringResource(id = R.string.no_data_access),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = "error",
                    tint = SurfaceColor.Warning,
                )
            },
            color = SurfaceColor.Warning,
            backgroundColor = SurfaceColor.WarningContainer,
        ),
    )
}

@Composable
fun ProgramItem(
    modifier: Modifier,
    program: ProgramUiModel,
    programLayout: ProgramLayout,
    verticalPadding: Dp,
    onSizeChanged: (IntSize) -> Unit,
    onItemClick: (programUiModel: ProgramUiModel) -> Unit,
    onGranularSyncClick: (programUiModel: ProgramUiModel) -> Unit,
) {
    val title = ListCardTitleModel(
        text = program.title,
        color = TextColor.OnPrimaryContainer.copy(
            alpha = program.getAlphaValue(),
        ),
    )

    val lastUpdated = program.lastUpdated.toDateSpan(LocalContext.current)

    val description = ListCardDescriptionModel(text = program.countDescription())

    when (programLayout) {
        ProgramLayout.DEFAULT ->
            ListCard(
                modifier = modifier,
                listCardState = rememberListCardState(
                    title = title,
                    lastUpdated = lastUpdated,
                    description = description,
                    loading = program.isDownloading(),
                    additionalInfoColumnState = rememberAdditionalInfoColumnState(
                        additionalInfoList = buildList {
                            program.description?.let { description ->
                                add(
                                    AdditionalInfoItem(
                                        value = description,
                                        color = TextColor.OnSurfaceLight,
                                    ),
                                )
                            }
                            addIf(
                                listOf(
                                    State.TO_POST,
                                    State.TO_UPDATE,
                                    State.ERROR,
                                    State.WARNING,
                                ).contains(program.state),
                                stateAdditionalInfoItem(program.state),
                            )
                        },
                        syncProgressItem = syncingAdditionalInfoItem(program),
                        expandLabelText = stringResource(R.string.show_description),
                        shrinkLabelText = stringResource(R.string.hide_description),
                        minItemsToShow = 0,
                    ),
                    expandable = true,
                    itemVerticalPadding = verticalPadding,
                ),
                listAvatar = {
                    ProgramAvatar(
                        program = program,
                        avatarSize = programLayout.metadataAvatarSize(),
                    )
                },

                onCardClick = { onItemClick(program) },
                actionButton = {
                    if (!program.isDownloading()) {
                        ProvideSyncButton(state = program.state) {
                            onGranularSyncClick(program)
                        }
                    }
                },
                onSizeChanged = onSizeChanged,
            )

        else ->
            VerticalInfoListCard(
                modifier = modifier,
                listCardState = rememberListCardState(
                    title = title,
                    lastUpdated = lastUpdated,
                    description = description,
                    additionalInfoColumnState = rememberAdditionalInfoColumnState(
                        additionalInfoList = buildList {
                            program.description?.let { description ->
                                add(
                                    AdditionalInfoItem(
                                        value = description,
                                        color = TextColor.OnSurfaceLight,
                                    ),
                                )
                            }
                            addIf(
                                listOf(
                                    State.TO_POST,
                                    State.TO_UPDATE,
                                    State.ERROR,
                                    State.WARNING,
                                ).contains(program.state),
                                stateAdditionalInfoItem(program.state),
                            )
                        },
                        expandLabelText = stringResource(R.string.show_description),
                        shrinkLabelText = stringResource(R.string.hide_description),
                        syncProgressItem = syncingAdditionalInfoItem(program),
                        minItemsToShow = 0,
                    ),
                    expandable = true,
                    itemVerticalPadding = verticalPadding,
                    loading = program.isDownloading(),
                ),
                listAvatar = {
                    ProgramAvatar(
                        program = program,
                        avatarSize = programLayout.metadataAvatarSize(),
                    )
                },
                onCardClick = { onItemClick(program) },
                actionButton = {
                    if (!program.isDownloading()) {
                        ProvideSyncButton(state = program.state) {
                            onGranularSyncClick(program)
                        }
                    }
                },
                onSizeChanged = onSizeChanged,
            )
    }
}

@Composable
private fun syncingAdditionalInfoItem(program: ProgramUiModel) = AdditionalInfoItem(
    icon = {
        when (program.downloadState) {
            ProgramDownloadState.DOWNLOADING ->
                ProgressIndicator(type = ProgressIndicatorType.CIRCULAR_SMALL)

            ProgramDownloadState.DOWNLOADED ->
                DownloadedIcon()

            ProgramDownloadState.ERROR ->
                DownloadErrorIcon()

            ProgramDownloadState.NONE -> {}
        }
    },
    value = when (program.downloadState) {
        ProgramDownloadState.DOWNLOADING ->
            stringResource(id = R.string.syncing, program.typeName)

        ProgramDownloadState.DOWNLOADED ->
            program.countDescription()

        ProgramDownloadState.ERROR ->
            program.countDescription()

        ProgramDownloadState.NONE ->
            ""
    },
    color = when (program.downloadState) {
        ProgramDownloadState.DOWNLOADING ->
            SurfaceColor.Primary

        ProgramDownloadState.DOWNLOADED ->
            SurfaceColor.CustomGreen

        ProgramDownloadState.ERROR ->
            TextColor.OnErrorContainer

        ProgramDownloadState.NONE ->
            TextColor.OnSurfaceLight
    },
    isConstantItem = true,
)

@Composable
private fun stateAdditionalInfoItem(state: State) = AdditionalInfoItem(
    icon = {
        StateIcon(
            state = state,
            enabled = false,
        ) {
            // no-op
        }
    },
    value = when (state) {
        State.TO_POST,
        State.TO_UPDATE,
        -> stringResource(id = R.string.not_synced)

        State.ERROR -> stringResource(id = R.string.sync_error_title)
        State.WARNING -> stringResource(id = R.string.sync_warning)
        else -> stringResource(id = R.string.sync_dialog_title_synced)
    },
    color = when (state) {
        State.ERROR -> TextColor.OnErrorContainer
        State.WARNING -> TextColor.OnWarningContainer
        else -> TextColor.OnSurfaceLight
    },
    isConstantItem = true,
)

@Composable
private fun ProgramAvatar(program: ProgramUiModel, avatarSize: MetadataAvatarSize) {
    Avatar(
        style = AvatarStyleData.Metadata(
            imageCardData = program.metadataIconData.imageCardData,
            avatarSize = avatarSize,
            tintColor = program.metadataIconData.color.copy(
                alpha = program.getAlphaValue(),
            ),
        ),
    )
}

@Composable
private fun ProvideSyncButton(
    state: State?,
    onSyncIconClick: () -> Unit,
) {
    val buttonText = when (state) {
        State.TO_POST,
        State.TO_UPDATE,
        -> {
            stringResource(R.string.sync)
        }

        State.ERROR,
        State.WARNING,
        -> {
            stringResource(R.string.sync_retry)
        }

        else -> null
    }
    buttonText?.let {
        Button(
            style = ButtonStyle.TONAL,
            text = it,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Sync,
                    contentDescription = it,
                    tint = TextColor.OnPrimaryContainer,
                )
            },
            onClick = { onSyncIconClick() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListPreview() {
    ProgramList(
        programs = listOf(
            testingProgramModel().copy(state = State.WARNING),
            testingProgramModel().copy(state = State.ERROR),
            testingProgramModel().copy(state = State.SYNCED),
            testingProgramModel().copy(state = State.TO_POST),
            testingProgramModel().copy(state = State.TO_UPDATE),
            testingProgramModel().copy(state = State.SYNCED_VIA_SMS),
            testingProgramModel().copy(state = State.SENT_VIA_SMS),
        ),
        onItemClick = {},
        onGranularSyncClick = {},
        downLoadState = SyncStatusData(
            true,
            downloadingMedia = true,
            programSyncStatusMap = emptyMap(),
        ),
    )
}

private fun testingProgramModel() = ProgramUiModel(
    uid = "qweqwe",
    title = "Program title",
    metadataIconData = MetadataIconData(
        imageCardData = ImageCardData.IconCardData(
            uid = "",
            label = "",
            iconRes = "dhis2_positive_negative",
            iconTint = "#00BCD4".toColor(),
        ),
        color = "#00BCD4".toColor(),
    ),
    count = 12,
    type = "type",
    typeName = "Persons",
    programType = "WITH_REGISTRATION",
    description = null,
    onlyEnrollOnce = false,
    accessDataWrite = true,
    state = State.SYNCED,
    downloadState = ProgramDownloadState.NONE,
    stockConfig = null,
    lastUpdated = Date(),
)

const val HOME_ITEMS = "HOME_ITEMS"
const val HOME_ITEM = "HOME_ITEMS_%s"
