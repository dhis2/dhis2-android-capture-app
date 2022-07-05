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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.ui.MetadataIcon
import org.dhis2.commons.ui.MetadataIconData
import org.hisp.dhis.android.core.common.State

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramList(
    programs: List<ProgramViewModel>,
    onItemClick: (programViewModel: ProgramViewModel) -> Unit,
    onGranularSyncClick: (programViewModel: ProgramViewModel) -> Unit
) {
    val conf = LocalConfiguration.current
    when (conf.orientation) {
        Configuration.ORIENTATION_LANDSCAPE ->
            LazyVerticalGrid(
                cells = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 56.dp)
            ) {
                items(items = programs) { program ->
                    ProgramItem(
                        programViewModel = program,
                        onItemClick = onItemClick,
                        onGranularSyncClick = onGranularSyncClick
                    )
                    Divider(
                        color = colorResource(id = R.color.divider_bg),
                        thickness = 1.dp,
                        startIndent = 72.dp
                    )
                }
            }
        else ->
            LazyColumn(
                modifier = Modifier.testTag(HOME_ITEMS),
                contentPadding = PaddingValues(bottom = 56.dp)
            ) {
                itemsIndexed(items = programs) { index, program ->
                    ProgramItem(
                        modifier = Modifier.semantics { testTag = HOME_ITEM.format(index) },
                        programViewModel = program,
                        onItemClick = onItemClick,
                        onGranularSyncClick = onGranularSyncClick
                    )
                    Divider(
                        color = colorResource(id = R.color.divider_bg),
                        thickness = 1.dp,
                        startIndent = 72.dp
                    )
                }
            }
    }
}

@Composable
fun ProgramItem(
    modifier: Modifier = Modifier,
    programViewModel: ProgramViewModel,
    onItemClick: (programViewModel: ProgramViewModel) -> Unit = {},
    onGranularSyncClick: (programViewModel: ProgramViewModel) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !programViewModel.isDownloading()) {
                onItemClick(programViewModel)
            }
            .background(color = Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetadataIcon(
            modifier = Modifier.alpha(programViewModel.getAlphaValue()),
            metadataIconData = programViewModel.metadataIconData
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(programViewModel.getAlphaValue())
        ) {
            Text(
                text = programViewModel.title,
                color = colorResource(id = R.color.textPrimary),
                fontSize = 14.sp,
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily(Font(R.font.rubik_regular))
                )
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
                    fontFamily = FontFamily(Font(R.font.rubik_regular))
                )
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
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun StateIcon(state: State, onClick: () -> Unit) {
    if (state != State.RELATIONSHIP && state != State.SYNCED) {
        IconButton(onClick = onClick) {
            Icon(
                painter = when (state) {
                    State.TO_POST,
                    State.TO_UPDATE,
                    State.UPLOADING -> painterResource(id = R.drawable.ic_sync_problem_grey)
                    State.ERROR -> painterResource(id = R.drawable.ic_sync_problem_red)
                    State.WARNING -> painterResource(id = R.drawable.ic_sync_warning)
                    State.SENT_VIA_SMS,
                    State.SYNCED_VIA_SMS -> painterResource(id = R.drawable.ic_sync_sms)
                    else -> painterResource(id = R.drawable.ic_status_synced)
                },
                contentDescription = "sync"
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
            ColorUtils.getPrimaryColor(LocalContext.current, ColorUtils.ColorType.PRIMARY)
        ),
        strokeWidth = 2.dp
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
                }
            )
        ),
        exit = shrinkOut(shrinkTowards = Alignment.Center)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_download_done),
            contentDescription = "downloaded",
            tint = Color.Unspecified
        )
    }
}

@Composable
fun DownloadErrorIcon(onClick: () -> Unit) {
    Icon(
        modifier = Modifier.clickable { onClick() },
        painter = painterResource(id = R.drawable.ic_download_error),
        contentDescription = "download error",
        tint = Color.Unspecified
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

@Preview
@Composable
fun ProgramTest() {
    ProgramItem(
        programViewModel = testingProgramModel()
    )
}

@Preview
@Composable
fun ProgramTestToPost() {
    ProgramItem(
        programViewModel = testingProgramModel().copy(state = State.TO_POST)
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
        }
    )
}

private fun testingProgramModel() = ProgramViewModel(
    uid = "qweqwe",
    title = "Program title",
    metadataIconData = MetadataIconData(
        programColor = android.graphics.Color.parseColor("#00BCD4"),
        iconResource = R.drawable.ic_positive_negative
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
    downloadState = ProgramDownloadState.NONE
)

const val HOME_ITEMS = "HOME_ITEMS"
const val HOME_ITEM = "HOME_ITEMS_%s"
