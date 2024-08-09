package org.dhis2.usescases.teiDashboard.ui.mapper

import android.graphics.BitmapFactory
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PhoneEnabled
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import org.dhis2.R
import org.dhis2.commons.date.toUi
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.DashboardEnrollmentModel
import org.dhis2.usescases.teiDashboard.DashboardModel
import org.dhis2.usescases.teiDashboard.DashboardTEIModel
import org.dhis2.usescases.teiDashboard.ui.model.TeiCardUiModel
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.mobile.ui.designsystem.Avatar
import org.hisp.dhis.mobile.ui.designsystem.AvatarStyle
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import java.io.File
import java.util.Date

class TeiDashboardCardMapper(
    val resourceManager: ResourceManager,
) {

    fun map(
        dashboardModel: DashboardModel,
        onImageClick: (File) -> Unit,
        phoneCallback: (String) -> Unit,
        emailCallback: (String) -> Unit,
        programsCallback: () -> Unit,
    ): TeiCardUiModel {
        val avatar: @Composable (() -> Unit)? =
            dashboardModel.avatarPath?.takeIf { it.isNotEmpty() }?.let {
                {
                    ProvideAvatar(path = it, onImageClick)
                }
            }

        return TeiCardUiModel(
            avatar = avatar,
            title = getTitle(dashboardModel),
            additionalInfo = getAdditionalInfo(
                dashboardModel,
                phoneCallback,
                emailCallback,
                programsCallback,
            ),
            actionButton = {},
            expandLabelText = resourceManager.getString(R.string.show_more),
            shrinkLabelText = resourceManager.getString(R.string.show_less),
            showLoading = false,
        )
    }

    @Composable
    private fun ProvideAvatar(path: String, onImageClick: (File) -> Unit) {
        val file = File(path)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
        val painter = BitmapPainter(bitmap)

        Avatar(
            imagePainter = painter,
            onImageClick = { onImageClick.invoke(file) },
            style = AvatarStyle.IMAGE,
        )
    }

    private fun getTitle(item: DashboardModel): String {
        return when {
            item.teiHeader != null -> item.teiHeader!!
            item is DashboardEnrollmentModel -> item.trackedEntityAttributes.takeIf { it.isNotEmpty() }
                ?.let {
                    val attribute = it.filterAttributes().firstOrNull()
                    val key = attribute?.first?.displayFormName()
                    val value = attribute?.second?.value()?.takeIf { attrValue ->
                        attrValue.isNotEmpty()
                    } ?: "-"
                    "$key: $value"
                } ?: "-"

            else -> "-"
        }
    }

    private fun getAdditionalInfo(
        item: DashboardModel,
        phoneCallback: (String) -> Unit,
        emailCallback: (String) -> Unit,
        programsCallback: () -> Unit,
    ): List<AdditionalInfoItem> {
        val attributesList = when (item) {
            is DashboardEnrollmentModel -> item.trackedEntityAttributes.filterAttributes()
            is DashboardTEIModel -> emptyList()
        }.map {
            if (it.first.valueType() == ValueType.PHONE_NUMBER) {
                AdditionalInfoItem(
                    key = it.first.displayFormName(),
                    value = it.second.value()?.takeIf { attrValue -> attrValue.isNotEmpty() }
                        ?: "-",
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PhoneEnabled,
                            contentDescription = "Icon Button",
                            tint = SurfaceColor.Primary,
                        )
                    },
                    color = SurfaceColor.Primary,
                    action = { phoneCallback.invoke(it.second.value() ?: "") },
                )
            } else if (it.first.valueType() == ValueType.EMAIL) {
                AdditionalInfoItem(
                    key = it.first.displayFormName(),
                    value = it.second.value() ?: "-",
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.MailOutline,
                            contentDescription = "Icon Button",
                            tint = SurfaceColor.Primary,
                        )
                    },
                    color = SurfaceColor.Primary,
                    action = { emailCallback.invoke(it.second.value() ?: "") },
                )
            } else {
                AdditionalInfoItem(
                    key = it.first.displayFormName(),
                    value = it.second.value() ?: "-",
                )
            }
        }.toMutableList()

        if (item.teiHeader == null) {
            attributesList.removeFirstOrNull()
        }

        return when (item) {
            is DashboardEnrollmentModel ->
                attributesList.also { list ->
                    if (item.currentProgram().displayIncidentDate() == true) {
                        addIncidentDate(
                            list,
                            item.currentProgram().incidentDateLabel(),
                            item.currentEnrollment.incidentDate(),
                        )
                    }
                }.also { list ->
                    addEnrollmentDate(
                        item.currentProgram().uid(),
                        list,
                        item.currentProgram().enrollmentDateLabel(),
                        item.currentEnrollment.enrollmentDate(),
                    )
                }.also { list ->
                    if (item.orgUnits.isNotEmpty()) {
                        addEnrollIn(
                            list,
                            item.getCurrentOrgUnit(),
                        )
                    }
                }.also { list ->
                    item.getEnrollmentActivePrograms().takeIf { it.isNotEmpty() }?.let {
                        addEnrollPrograms(
                            list,
                            it,
                            programsCallback,
                        )
                    }
                }

            is DashboardTEIModel -> attributesList
        }
    }

    private fun addEnrollPrograms(
        list: MutableList<AdditionalInfoItem>,
        programs: List<Program>,
        programsCallback: () -> Unit,
    ) {
        val names = programs.map { it.displayName() }

        list.add(
            AdditionalInfoItem(
                key = resourceManager.getString(R.string.programs),
                value = names.joinToString(", "),
                isConstantItem = true,
                action = { programsCallback.invoke() },
            ),
        )
    }

    private fun addEnrollIn(
        list: MutableList<AdditionalInfoItem>,
        currentOrgUnit: OrganisationUnit?,
    ) {
        list.add(
            AdditionalInfoItem(
                key = resourceManager.getString(R.string.enrolledIn),
                value = currentOrgUnit?.displayName() ?: "-",
                isConstantItem = true,
            ),
        )
    }

    private fun addIncidentDate(
        list: MutableList<AdditionalInfoItem>,
        incidentDateLabel: String?,
        incidentDate: Date?,
    ) {
        list.add(
            AdditionalInfoItem(
                key = incidentDateLabel ?: resourceManager.getString(R.string.incident_date),
                value = incidentDate.toUi() ?: "-",
                isConstantItem = true,
            ),
        )
    }

    private fun addEnrollmentDate(
        programUid: String,
        list: MutableList<AdditionalInfoItem>,
        programLabel: String?,
        enrollmentDate: Date?,
    ) {
        list.add(
            AdditionalInfoItem(
                key = programLabel ?: resourceManager.formatWithEnrollmentLabel(
                    programUid,
                    R.string.enrollment_date_V2,
                    1,
                ),
                value = enrollmentDate.toUi() ?: "-",
                isConstantItem = true,
            ),
        )
    }

    private fun List<Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>>.filterAttributes() =
        this.filter { it.first.valueType() != ValueType.IMAGE }
            .filter { it.first.valueType() != ValueType.COORDINATE }
            .filter { it.first.valueType() != ValueType.FILE_RESOURCE }
            .filter { it.second.value()?.isNotEmpty() == true }
}
