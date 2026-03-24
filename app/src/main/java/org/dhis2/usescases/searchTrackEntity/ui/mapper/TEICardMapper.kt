package org.dhis2.usescases.searchTrackEntity.ui.mapper

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import org.dhis2.R
import org.dhis2.commons.bindings.isFilePathValid
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.date.toOverdueOrScheduledUiText
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.ui.model.ListCardUiModel
import org.dhis2.mobile.commons.extensions.toJavaDate
import org.dhis2.tracker.search.model.DomainEnrollment
import org.dhis2.tracker.search.model.DomainProgram
import org.dhis2.tracker.search.model.EnrollmentStatus
import org.dhis2.tracker.search.model.SyncState
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel
import org.dhis2.usescases.searchTrackEntity.adapters.hasFollowUp
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyleData
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatarSize
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
    ): ListCardUiModel =
        ListCardUiModel(
            avatar = { ProvideAvatar(searchTEIModel, onImageClick) },
            title = getTitle(searchTEIModel),
            lastUpdated = searchTEIModel.tei.lastUpdated?.toJavaDate().toDateSpan(context),
            additionalInfo = getAdditionalInfoList(searchTEIModel),
            actionButton = { ProvideSyncButton(searchTEIModel, onSyncIconClick) },
            expandLabelText = resourceManager.getString(R.string.show_more),
            shrinkLabelText = resourceManager.getString(R.string.show_less),
            onCardCLick = onCardClick,
        )

    @Composable
    private fun ProvideAvatar(
        item: SearchTeiModel,
        onImageClick: ((String) -> Unit),
    ) {
        val programUid: String? =
            if (item.selectedEnrollment != null) {
                item.selectedEnrollment.program
            } else {
                null
            }

        if (isFilePathValid(item.profilePicturePath)) {
            val file = File(item.profilePicturePath)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
            val painter = BitmapPainter(bitmap)

            Avatar(
                style = AvatarStyleData.Image(painter),
                onImageClick = { onImageClick(item.profilePicturePath) },
            )
        } else if (item.isMetadataIconDataAvailable(programUid)) {
            Avatar(
                style =
                    AvatarStyleData.Metadata(
                        imageCardData = item.getMetadataIconData(programUid).imageCardData,
                        avatarSize = MetadataAvatarSize.S(),
                        tintColor = item.getMetadataIconData(programUid).color,
                    ),
            )
        } else {
            Avatar(
                style = AvatarStyleData.Text(getTitleFirstLetter(item)),
            )
        }
    }

    private fun getTitleFirstLetter(item: SearchTeiModel): String {
        val firstLetter =
            item.header?.firstOrNull()
                ?: item.attributeValues.values
                    .firstOrNull()
                    ?.value
                    ?.firstOrNull()

        return when (firstLetter) {
            null -> "?"
            '-' -> "?"
            else -> firstLetter.uppercaseChar().toString()
        }
    }

    private fun getTitle(item: SearchTeiModel): String =
        item.header ?: run {
            val key = item.attributeValues.keys.firstOrNull() ?: "-"
            val value =
                item.attributeValues.values
                    .firstOrNull()
                    ?.value ?: "-"
            "$key: $value"
        }

    private fun getAdditionalInfoList(searchTEIModel: SearchTeiModel): List<AdditionalInfoItem> {
        val attributeList =
            searchTEIModel.tei.attributeValues
                .map {
                    AdditionalInfoItem(
                        key = it.displayName,
                        value = it.value ?: "-",
                    )
                }.toMutableList()

        if (searchTEIModel.header == null) {
            attributeList.removeFirstOrNull()
        }
        attributeList.removeIf { it.value.isEmpty() || it.value == "-" }

        return attributeList.also { list ->
            searchTEIModel.tei.ownerOrgUnit?.let {
                if (it != searchTEIModel.tei.enrollmentOrgUnit) {
                    addOwnedBy(
                        list = list,
                        ownerOrgUnit = it,
                    )
                }
            }
            if (searchTEIModel.tei.shouldDisplayOrgUnit) {
                checkEnrolledIn(
                    list = list,
                    enrolledOrgUnit = searchTEIModel.tei.enrollmentOrgUnit,
                )
            }

            checkEnrolledPrograms(
                list = list,
                enrolledPrograms = searchTEIModel.tei.enrolledPrograms,
            )
            val programUid: String? =
                if (searchTEIModel.selectedEnrollment != null) {
                    searchTEIModel.selectedEnrollment.program
                } else {
                    null
                }
            checkEnrollmentStatus(
                programUid = programUid,
                list = list,
                status = searchTEIModel.selectedEnrollment?.status?: EnrollmentStatus.ACTIVE,
            )

            checkOverdue(
                list = list,
                hasOverdue = searchTEIModel.tei.overDueDate != null,
                overdueDate = searchTEIModel.tei.overDueDate?.toJavaDate(),
            )

            checkFollowUp(
                list = list,
                enrollments = searchTEIModel.tei.enrollments,
            )

            checkSyncStatus(
                list = list,
                state = searchTEIModel.tei.syncState,
            )
        }
    }

    private fun checkFollowUp(
        list: MutableList<AdditionalInfoItem>,
        enrollments: List<DomainEnrollment>?,
    ) {
        enrollments?.let {
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

    }

    private fun checkEnrollmentStatus(
        programUid: String?,
        list: MutableList<AdditionalInfoItem>,
        status: EnrollmentStatus,
    ) {
        val item =
            when (status) {
                EnrollmentStatus.COMPLETED -> {
                    val label =
                        resourceManager.formatWithEnrollmentLabel(
                            programUid,
                            R.string.enrollment_completed_V2,
                            1,
                        )
                    AdditionalInfoItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = label,
                                tint = AdditionalInfoItemColor.SUCCESS.color,
                            )
                        },
                        value = label,
                        isConstantItem = true,
                        color = AdditionalInfoItemColor.SUCCESS.color,
                    )
                }

                EnrollmentStatus.CANCELLED -> {
                    val label =
                        resourceManager.formatWithEnrollmentLabel(
                            programUid,
                            R.string.enrollment_cancelled_V2,
                            1,
                        )
                    AdditionalInfoItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = label,
                                tint = AdditionalInfoItemColor.DISABLED.color,
                            )
                        },
                        value = label,
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
        enrolledPrograms: List<DomainProgram>?,
    ) {
        val programNames = enrolledPrograms?.map { it.displayName }

        programNames?.let {
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
        enrolledOrgUnit: String?,
    ) {
        enrolledOrgUnit?.let {
            list.add(
                AdditionalInfoItem(
                    key = resourceManager.getString(R.string.enrolledIn),
                    value = it,
                    isConstantItem = true,
                ),
            )
        }

    }

    private fun addOwnedBy(
        list: MutableList<AdditionalInfoItem>,
        ownerOrgUnit: String,
    ) {
        list.add(
            AdditionalInfoItem(
                key = resourceManager.getString(R.string.ownedBy),
                value = ownerOrgUnit,
                isConstantItem = true,
            ),
        )
    }

    @Composable
    private fun ProvideSyncButton(
        searchTEIModel: SearchTeiModel,
        onSyncIconClick: () -> Unit,
    ) {
        val buttonText =
            when (searchTEIModel.tei.syncState) {
                SyncState.TO_POST,
                SyncState.TO_UPDATE,
                    -> {
                    resourceManager.getString(R.string.sync)
                }

                SyncState.ERROR,
                SyncState.WARNING,
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
            val text = overdueDate.toOverdueOrScheduledUiText(resourceManager)
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
        state: SyncState?,
    ) {
        val item =
            when (state) {
                SyncState.TO_POST,
                SyncState.TO_UPDATE,
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

                SyncState.UPLOADING -> {
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

                SyncState.ERROR -> {
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

                SyncState.WARNING -> {
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
