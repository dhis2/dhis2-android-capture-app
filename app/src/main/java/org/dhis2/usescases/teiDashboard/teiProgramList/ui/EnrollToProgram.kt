package org.dhis2.usescases.teiDashboard.teiProgramList.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import org.dhis2.usescases.main.program.ProgramUiModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.internal.ImageCardData
import java.util.Date

@Composable
fun EnrollToProgram(programUiModel: ProgramUiModel, onEnrollClickListener: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(start = 21.dp, top = 8.dp, end = 21.dp, bottom = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetadataIcon(
                modifier = Modifier
                    .width(56.dp)
                    .height(56.dp)
                    .alpha(0.5f),
                metadataIconData = programUiModel.metadataIconData,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier
                    .weight(2f, true)
                    .padding(end = 12.dp),
                text = programUiModel.title,
                fontSize = 14.sp,
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 16.dp, end = 16.dp).fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.width(68.dp).height(0.dp))

            Button(
                text = stringResource(id = R.string.enroll),
                modifier = Modifier.fillMaxWidth()
                    .semantics { testTag = PROGRAM_TO_ENROLL.format(programUiModel.title) },
                enabled = !programUiModel.isDownloading(),
                onClick = onEnrollClickListener,
                style = ButtonStyle.TONAL,
            )
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

private fun testingProgramModel(downloadState: ProgramDownloadState) = ProgramUiModel(
    uid = "qweqwe",
    title = "A very long long long program title",
    metadataIconData = MetadataIconData(
        imageCardData = ImageCardData.IconCardData(
            uid = "7e0cb105-c276-4f12-9f56-a26af8314121",
            label = "Stethoscope",
            iconRes = "dhis2_stethoscope_positive",
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
    lastUpdated = Date(),
)

const val PROGRAM_TO_ENROLL = "PROGRAM_TO_ENROLL_%s"
