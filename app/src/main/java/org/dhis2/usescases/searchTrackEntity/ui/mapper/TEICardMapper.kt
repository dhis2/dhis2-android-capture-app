package org.dhis2.usescases.searchTrackEntity.ui.mapper

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import org.dhis2.R
import org.dhis2.bindings.hasFollowUp
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.date.toOverdueUiText
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyle
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import java.io.File
import java.util.Date

class TEICardMapper(
    val context: Context,
    val resourceManager: ResourceManager,
) {

    fun map(
        searchTEIModel: SearchTeiModel,
        onSyncIconClick: () -> Unit,
        onCardClick: () -> Unit,
        onImageClick: (String) -> Unit,
    ): ListCardUiModel {
        return ListCardUiModel(
            avatar = { ProvideAvatar(searchTEIModel, onImageClick) },
            title = getTitle(searchTEIModel),
            lastUpdated = searchTEIModel.tei.lastUpdated().toDateSpan(context),
            additionalInfo = getAdditionalInfoList(searchTEIModel),
            actionButton = { ProvideSyncButton(searchTEIModel, onSyncIconClick) },
            expandLabelText = resourceManager.getString(R.string.show_more),
            shrinkLabelText = resourceManager.getString(R.string.show_less),
            onCardCLick = onCardClick,
        )
    }

    @Composable
    private fun ProvideAvatar(item: SearchTeiModel, onImageClick: ((String) -> Unit)) {
        if (item.profilePicturePath.isNotEmpty()) {
            val file = File(item.profilePicturePath)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
            val painter = BitmapPainter(bitmap)

            Avatar(
                imagePainter = painter,
                style = AvatarStyle.IMAGE,
                onImageClick = { onImageClick(item.profilePicturePath) },
            )
        } else {
            Avatar(
                textAvatar = getTitleFirstLetter(item),
                style = AvatarStyle.TEXT,
            )
        }
    }

    private fun getTitleFirstLetter(item: SearchTeiModel): String {
        val firstLetter = item.header?.firstOrNull()
            ?: item.attributeValues.values.firstOrNull()?.value()?.firstOrNull()

        return when (firstLetter) {
            null -> "?"
            '-' -> "?"
            else -> firstLetter.uppercaseChar().toString()
        }
    }

    private fun getTitle(item: SearchTeiModel): String {
        return if (item.header != null) {
            item.header!!
        } else if (item.attributeValues.isEmpty()) {
            "-"
        } else {
            val key = item.attributeValues.keys.firstOrNull()
            val value = item.attributeValues.values.firstOrNull()?.value()
            "$key: $value"
        }
    }

    private fun getAdditionalInfoList(searchTEIModel: SearchTeiModel): List<AdditionalInfoItem> {
        val attributeList = searchTEIModel.attributeValues.map {
            AdditionalInfoItem(
                key = "${it.key}:",
                value = it.value.value() ?: "",
            )
        }.toMutableList()

        if (searchTEIModel.header == null) {
            attributeList.removeFirstOrNull()
        }
        attributeList.removeIf { it.value.isEmpty() || it.value == "-" }

        return attributeList.also { list ->
            checkEnrolledIn(
                list = list,
                enrolledOrgUnit = searchTEIModel.enrolledOrgUnit,
            )
            checkEnrolledPrograms(
                list = list,
                enrolledPrograms = searchTEIModel.programInfo,
            )
            checkEnrollmentStatus(
                list = list,
                status = searchTEIModel.selectedEnrollment?.status(),
            )

            checkOverdue(
                list = list,
                hasOverdue = searchTEIModel.isHasOverdue,
                overdueDate = searchTEIModel.overdueDate,
            )

            checkFollowUp(
                list = list,
                enrollments = searchTEIModel.enrollments,
            )

            checkSyncStatus(
                list = list,
                state = searchTEIModel.tei.aggregatedSyncState(),
            )
        }
    }

    private fun checkFollowUp(
        list: MutableList<AdditionalInfoItem>,
        enrollments: List<Enrollment>,
    ) {
        if (enrollments.hasFollowUp()) {
            list.add(
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = resourceManager.getString(R.string.marked_follow_up),
                            tint = AdditionalInfoItemColor.WARNING.color,
                        )
                    },
                    value = resourceManager.getString(R.string.marked_follow_up),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.WARNING.color,
                ),
            )
        }
    }

    private fun checkEnrollmentStatus(
        list: MutableList<AdditionalInfoItem>,
        status: EnrollmentStatus?,
    ) {
        val item = when (status) {
            EnrollmentStatus.COMPLETED -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = resourceManager.getString(R.string.enrollment_completed),
                            tint = AdditionalInfoItemColor.SUCCESS.color,
                        )
                    },
                    value = resourceManager.getString(R.string.enrollment_completed),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.SUCCESS.color,
                )
            }

            EnrollmentStatus.CANCELLED -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = resourceManager.getString(R.string.enrollment_cancelled),
                            tint = AdditionalInfoItemColor.DISABLED.color,
                        )
                    },
                    value = resourceManager.getString(R.string.enrollment_cancelled),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.DISABLED.color,
                )
            }

            else -> null
        }

        item?.let { list.add(it) }
    }

    private fun checkEnrolledPrograms(
        list: MutableList<AdditionalInfoItem>,
        enrolledPrograms: List<Program>,
    ) {
        val programNames = enrolledPrograms.map { it.name() }

        if (programNames.isNotEmpty()) {
            list.add(
                AdditionalInfoItem(
                    key = resourceManager.getString(R.string.programs),
                    value = programNames.joinToString(", "),
                    isConstantItem = true,
                ),
            )
        }
    }

    private fun checkEnrolledIn(
        list: MutableList<AdditionalInfoItem>,
        enrolledOrgUnit: String,
    ) {
        list.add(
            AdditionalInfoItem(
                key = resourceManager.getString(R.string.enrolledIn),
                value = enrolledOrgUnit,
                isConstantItem = true,
            ),
        )
    }

    @Composable
    private fun ProvideSyncButton(searchTEIModel: SearchTeiModel, onSyncIconClick: () -> Unit) {
        val buttonText = when (searchTEIModel.tei.aggregatedSyncState()) {
            State.TO_POST,
            State.TO_UPDATE,
            -> {
                resourceManager.getString(R.string.sync)
            }

            State.ERROR,
            State.WARNING,
            -> {
                resourceManager.getString(R.string.sync_retry)
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

    private fun checkOverdue(
        list: MutableList<AdditionalInfoItem>,
        hasOverdue: Boolean,
        overdueDate: Date?,
    ) {
        if (hasOverdue) {
            val text = overdueDate.toOverdueUiText(resourceManager)
            list.add(
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.EventBusy,
                            contentDescription = text,
                            tint = AdditionalInfoItemColor.ERROR.color,
                        )
                    },
                    value = text,
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.ERROR.color,
                ),
            )
        }
    }

    private fun checkSyncStatus(
        list: MutableList<AdditionalInfoItem>,
        state: State?,
    ) {
        val item = when (state) {
            State.TO_POST,
            State.TO_UPDATE,
            -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncDisabled,
                            contentDescription = resourceManager.getString(R.string.not_synced),
                            tint = AdditionalInfoItemColor.DISABLED.color,
                        )
                    },
                    value = resourceManager.getString(R.string.not_synced),
                    color = AdditionalInfoItemColor.DISABLED.color,
                    isConstantItem = true,
                )
            }

            State.UPLOADING -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
                            contentDescription = resourceManager.getString(R.string.syncing),
                            tint = SurfaceColor.Primary,
                        )
                    },
                    value = resourceManager.getString(R.string.syncing),
                    color = SurfaceColor.Primary,
                    isConstantItem = true,
                )
            }

            State.ERROR -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncProblem,
                            contentDescription = resourceManager.getString(R.string.sync_error_title),
                            tint = AdditionalInfoItemColor.ERROR.color,
                        )
                    },
                    value = resourceManager.getString(R.string.sync_error_title),
                    color = AdditionalInfoItemColor.ERROR.color,
                    isConstantItem = true,
                )
            }

            State.WARNING -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncProblem,
                            contentDescription = resourceManager.getString(R.string.sync_dialog_title_warning),
                            tint = AdditionalInfoItemColor.WARNING.color,
                        )
                    },
                    value = resourceManager.getString(R.string.sync_dialog_title_warning),
                    color = AdditionalInfoItemColor.WARNING.color,
                    isConstantItem = true,
                )
            }

            else -> null
        }
        item?.let { list.add(it) }
    }
}
