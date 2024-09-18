package org.dhis2.usescases.teiDashboard.teiProgramList.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.R
import org.dhis2.ui.MetadataIcon
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.toColor
import org.dhis2.usescases.main.program.ProgramDownloadState
import org.dhis2.usescases.main.program.ProgramViewModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.internal.ImageCardData

@Composable
fun EnrollToProgram(programViewModel: ProgramViewModel, onEnrollClickListener: () -> Unit) {
    Row(
        modifier = Modifier
            .height(86.dp)
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(horizontal = 21.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MetadataIcon(
            modifier = Modifier
                .width(56.dp)
                .height(56.dp)
                .alpha(0.5f),
            metadataIconData = programViewModel.metadataIconData,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier
                .weight(2f, true)
                .padding(end = 12.dp),
            text = programViewModel.title,
            fontSize = 14.sp,
        )
        Button(
            modifier = Modifier
                .semantics { testTag = PROGRAM_TO_ENROLL.format(programViewModel.title) }
                .height(36.dp)
                .weight(1.2f, true)
                .padding(end = 16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.DarkGray,
                contentColor = Color.White,
            ),
            enabled = !programViewModel.isDownloading(),
            onClick = onEnrollClickListener,
        ) {
            Text(text = stringResource(id = R.string.enroll).uppercase())
        }
    }
}

@Preview
@Composable
fun EnrollToProgramEnabledPreview() {
    EnrollToProgram(testingProgramModel(ProgramDownloadState.DOWNLOADING)) {}
}

@Preview
@Composable
fun EnrollToProgramDisabledPreview() {
    EnrollToProgram(testingProgramModel(ProgramDownloadState.DOWNLOADED)) {}
}

private fun testingProgramModel(downloadState: ProgramDownloadState) = ProgramViewModel(
    uid = "qweqwe",
    title = "A very long long long program title",
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
    downloadState = downloadState,
    stockConfig = null,
)

const val PROGRAM_TO_ENROLL = "PROGRAM_TO_ENROLL_%s"
