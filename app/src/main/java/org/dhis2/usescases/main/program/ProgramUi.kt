package org.dhis2.usescases.main.program

import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.R
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.ui.icons.toIconData
import org.dhis2.data.service.SyncStatusData
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.toColor
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarSize
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBarData
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatar
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataIcon
import org.hisp.dhis.mobile.ui.designsystem.component.internal.ImageCardData
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
fun ProgramList(
    programs: List<ProgramViewModel>?,
    onItemClick: (programViewModel: ProgramViewModel) -> Unit,
    onGranularSyncClick: (programViewModel: ProgramViewModel) -> Unit,
    downLoadState: SyncStatusData?,
) {
    val conf = LocalConfiguration.current
    Column {
        AnimatedVisibility(
            visible = downLoadState?.downloadingMedia == true,
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
            DownloadMedia()
        }

        programs?.let {
            if (programs.isEmpty()) {
                NoAccessMessage()
            }

            when (conf.orientation) {
                Configuration.ORIENTATION_LANDSCAPE ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(bottom = 56.dp),
                    ) {
                        items(items = programs) { program ->
                            ProgramItem(
                                programViewModel = program,
                                onItemClick = onItemClick,
                                onGranularSyncClick = onGranularSyncClick,
                            )
                            Divider(
                                color = colorResource(id = R.color.divider_bg),
                                thickness = 1.dp,
                                startIndent = 72.dp,
                            )
                        }
                    }

                else ->
                    LazyColumn(
                        modifier = Modifier.testTag(HOME_ITEMS),
                        contentPadding = PaddingValues(bottom = 56.dp),
                    ) {
                        itemsIndexed(items = programs) { index, program ->
                            ProgramItem(
                                modifier = Modifier.semantics { testTag = HOME_ITEM.format(index) },
                                programViewModel = program,
                                onItemClick = onItemClick,
                                onGranularSyncClick = onGranularSyncClick,
                            )
                            Divider(
                                color = colorResource(id = R.color.divider_bg),
                                thickness = 1.dp,
                                startIndent = 72.dp,
                            )
                        }
                    }
            }
        }
    }
}

@Composable
fun ProgramItem(
    modifier: Modifier = Modifier,
    programViewModel: ProgramViewModel,
    onItemClick: (programViewModel: ProgramViewModel) -> Unit = {},
    onGranularSyncClick: (programViewModel: ProgramViewModel) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !programViewModel.isDownloading()) {
                onItemClick(programViewModel)
            }
            .background(color = Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
        ) {
            MetadataAvatar(
                icon = {
                    MetadataIcon(
                        imageCardData = programViewModel.metadataIconData.imageCardData,
                    )
                },
                iconTint = programViewModel.metadataIconData.color,
                size = AvatarSize.Normal,
            )

            var openDescriptionDialog by remember {
                mutableStateOf(false) // Initially dialog is closed
            }

            if (programViewModel.description != null) {
                ProgramDescriptionIcon {
                    openDescriptionDialog = true
                }
            }

            if (openDescriptionDialog) {
                ProgramDescriptionDialog(programViewModel.description ?: "") {
                    openDescriptionDialog = false
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(programViewModel.getAlphaValue()),
        ) {
            Text(
                text = programViewModel.title,
                color = colorResource(id = R.color.textPrimary),
                fontSize = 14.sp,
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily(Font(R.font.rubik_regular)),
                ),
            )
            Text(
                text = if (programViewModel.downloadState == ProgramDownloadState.DOWNLOADING) {
                    stringResource(R.string.syncing_resource, programViewModel.typeName.lowercase())
                } else {
                    programViewModel.countDescription()
                },
                color = colorResource(id = R.color.textSecondary),
                fontSize = 12.sp,
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily(Font(R.font.rubik_regular)),
                ),
            )
        }

        when (programViewModel.downloadState) {
            ProgramDownloadState.DOWNLOADING -> DownloadingProgress()
            ProgramDownloadState.DOWNLOADED -> DownloadedIcon(programViewModel)
            ProgramDownloadState.NONE -> StateIcon(programViewModel.state) {
                onGranularSyncClick(programViewModel)
            }

            ProgramDownloadState.ERROR -> DownloadErrorIcon {
                onGranularSyncClick(programViewModel)
            }
        }

        if (programViewModel.hasOverdueEvent) {
            Icon(
                painter = painterResource(id = R.drawable.ic_overdue),
                contentDescription = "Overdue",
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
fun StateIcon(state: State, onClick: () -> Unit) {
    if (state != State.RELATIONSHIP && state != State.SYNCED) {
        IconButton(onClick = onClick) {
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DownloadedIcon(programViewModel: ProgramViewModel) {
    val visible = visibility(programViewModel)
    AnimatedVisibility(
        visible = visible,
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
        Icon(
            painter = painterResource(id = R.drawable.ic_download_done),
            contentDescription = "downloaded",
            tint = Color.Unspecified,
        )
    }
}

@Composable
fun DownloadErrorIcon(onClick: () -> Unit) {
    Icon(
        modifier = Modifier.clickable { onClick() },
        painter = painterResource(id = R.drawable.ic_download_error),
        contentDescription = "download error",
        tint = Color.Unspecified,
    )
}

@Composable
fun visibility(viewModel: ProgramViewModel): Boolean {
    var visible by remember { mutableStateOf(!viewModel.hasShowCompleteSyncAnimation()) }
    DisposableEffect(Unit) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = {
            visible = false
            viewModel.setCompleteSyncAnimation()
        }
        handler.postDelayed(runnable, 3000)
        onDispose { handler.removeCallbacks(runnable) }
    }
    return visible
}

@Composable
fun ProgramDescriptionIcon(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 15.dp,
                    topEnd = 10.dp,
                    bottomEnd = 4.dp,
                    bottomStart = 15.dp,
                ),
            )
            .background(Color.White)
            .clickable { onClick() },
    ) {
        Icon(
            modifier = Modifier
                .size(16.dp)
                .padding(1.dp),
            painter = painterResource(id = R.drawable.ic_info),
            contentDescription = stringResource(id = R.string.program_description),
            tint = Color.Unspecified,
        )
    }
}

@Composable
fun ProgramDescriptionDialog(description: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = stringResource(R.string.info))
        },
        text = {
            Text(text = description)
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
            ) {
                Text(
                    text = stringResource(id = R.string.action_close).uppercase(),
                    color = colorResource(id = R.color.black_de0),
                )
            }
        },
    )
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

@Preview
@Composable
fun ProgramTest() {
    ProgramItem(
        programViewModel = testingProgramModel(),
    )
}

@Preview
@Composable
fun ProgramTestToPost() {
    ProgramItem(
        programViewModel = testingProgramModel().copy(state = State.TO_POST),
    )
}

@Preview
@Composable
fun ProgramTestWithDescription() {
    ProgramItem(
        programViewModel = testingProgramModel().copy(description = "Program description"),
    )
}

@Preview
@Composable
fun ProgramTestDownloaded() {
    var downloadState by remember {
        mutableStateOf(ProgramDownloadState.DOWNLOADING)
    }
    ProgramItem(
        programViewModel = testingProgramModel().copy(downloadState = downloadState),
        onItemClick = {
            downloadState = if (downloadState == ProgramDownloadState.DOWNLOADING) {
                ProgramDownloadState.DOWNLOADED
            } else {
                ProgramDownloadState.DOWNLOADING
            }
        },
    )
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
        downLoadState = SyncStatusData(true, true, emptyMap()),
    )
}

@Preview(showBackground = true)
@Composable
fun ProgramDescriptionDialogPReview() {
    ProgramDescriptionDialog(description = "Program description") { }
}

private fun testingProgramModel() = ProgramViewModel(
    uid = "qweqwe",
    title = "Program title",
    metadataIconData = MetadataIconData(
        imageCardData = ImageCardData.IconCardData(
            uid = "",
            label = "",
            iconRes = "ic_positive_negative",
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
    hasOverdueEvent = true,
    false,
    downloadState = ProgramDownloadState.NONE,
    stockConfig = null,
)

const val HOME_ITEMS = "HOME_ITEMS"
const val HOME_ITEM = "HOME_ITEMS_%s"
