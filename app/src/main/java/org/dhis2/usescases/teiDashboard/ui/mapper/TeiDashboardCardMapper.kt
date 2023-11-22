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
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.date.toUi
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.DashboardProgramModel
import org.dhis2.usescases.teiDashboard.ui.model.TeiCardUiModel
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import java.io.File
import java.util.Date

class TeiDashboardCardMapper(
    val resourceManager: ResourceManager,
) {

    fun map(
        dashboardModel: DashboardProgramModel,
        onImageClick: (File) -> Unit,
        phoneCallback: (String) -> Unit,
        emailCallback: (String) -> Unit,
        programsCallback: () -> Unit,
    ): TeiCardUiModel {
        val avatar: @Composable (() -> Unit)? = if (dashboardModel.avatarPath.isNotEmpty()) {
            { ProvideAvatar(item = dashboardModel, onImageClick) }
        } else {
            null
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
    private fun ProvideAvatar(item: DashboardProgramModel, onImageClick: (File) -> Unit) {
        val file = File(item.avatarPath)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
        val painter = BitmapPainter(bitmap)

        Avatar(
            imagePainter = painter,
            onImageClick = { onImageClick.invoke(file) },
            style = AvatarStyle.IMAGE,
        )
    }

    private fun getTitle(item: DashboardProgramModel): String {
        return if (item.teiHeader != null) {
            item.teiHeader!!
        } else if (item.attributes.isEmpty()) {
            "-"
        } else {
            val attribute = item.attributes.filterAttributes().firstOrNull()
            val key = attribute?.val0()?.displayFormName()
            val value = attribute?.val1()?.value()
            "$key: $value"
        }
    }

    private fun getAdditionalInfo(
        item: DashboardProgramModel,
        phoneCallback: (String) -> Unit,
        emailCallback: (String) -> Unit,
        programsCallback: () -> Unit,
    ): List<AdditionalInfoItem> {
        val attributesList = item.attributes
            .filterAttributes()
            .map {
                if (it.val0().valueType() == ValueType.PHONE_NUMBER) {
                    AdditionalInfoItem(
                        key = "${it.val0().displayFormName()}:",
                        value = it.val1().value() ?: "",
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.PhoneEnabled,
                                contentDescription = "Icon Button",
                                tint = SurfaceColor.Primary,
                            )
                        },
                        color = SurfaceColor.Primary,
                        action = { phoneCallback.invoke(it.val1().value() ?: "") },
                    )
                } else if (it.val0().valueType() == ValueType.EMAIL) {
                    AdditionalInfoItem(
                        key = "${it.val0().displayFormName()}:",
                        value = it.val1().value() ?: "",
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.MailOutline,
                                contentDescription = "Icon Button",
                                tint = SurfaceColor.Primary,
                            )
                        },
                        color = SurfaceColor.Primary,
                        action = { emailCallback.invoke(it.val1().value() ?: "") },
                    )
                } else {
                    AdditionalInfoItem(
                        key = "${it.val0().displayFormName()}:",
                        value = it.val1().value() ?: "",
                    )
                }
            }.toMutableList()

        if (item.teiHeader == null) {
            attributesList.removeFirstOrNull()
        }

        return attributesList.also { list ->
            if (item.currentProgram.displayIncidentDate() == true) {
                addIncidentDate(
                    list,
                    item.currentProgram.incidentDateLabel(),
                    item.currentEnrollment.incidentDate(),
                )
            }
        }.also { list ->
            addEnrollmentDate(
                list,
                item.currentProgram.enrollmentDateLabel(),
                item.currentEnrollment.enrollmentDate(),
            )
        }.also { list ->
            if (item.orgUnits.isNotEmpty()) {
                addEnrollIn(
                    list,
                    item.currentOrgUnit,
                )
            }
        }.also { list ->
            if (item.enrollmentActivePrograms.isNotEmpty()) {
                addEnrollPrograms(
                    list,
                    item.enrollmentActivePrograms,
                    programsCallback,
                )
            }
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
                value = currentOrgUnit?.displayName() ?: "",
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
                key = "${incidentDateLabel ?: resourceManager.getString(R.string.incident_date)}:",
                value = incidentDate.toUi() ?: "",
                isConstantItem = true,
            ),
        )
    }

    private fun addEnrollmentDate(
        list: MutableList<AdditionalInfoItem>,
        programLabel: String?,
        enrollmentDate: Date?,
    ) {
        list.add(
            AdditionalInfoItem(
                key = "${programLabel ?: resourceManager.getString(R.string.enrollment_date)}:",
                value = enrollmentDate.toUi() ?: "",
                isConstantItem = true,
            ),
        )
    }

    private fun List<Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>>.filterAttributes() =
        this.filter { it.val0().valueType() != ValueType.IMAGE }
            .filter { it.val0().valueType() != ValueType.COORDINATE }
            .filter { it.val0().valueType() != ValueType.FILE_RESOURCE }
            .filter { it.val1().value()?.isNotEmpty() == true }
}
