package org.dhis2.usescases.teiDashboard.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem

data class TeiCardUiModel(
    val avatar: (@Composable () -> Unit)? = null,
    val title: String,
    val additionalInfo: List<AdditionalInfoItem>,
    val actionButton: @Composable (() -> Unit),
    val expandLabelText: String,
    val shrinkLabelText: String,
    val showLoading: Boolean,
)

data class InfoBarUiModel(
    val type: InfoBarType,
    val currentEnrollment: Enrollment,
    val text: String,
    val textColor: Color,
    val icon: @Composable () -> Unit,
    val actionText: String?,
    val onActionClick: () -> Unit,
    val backgroundColor: Color,
    val showInfoBar: Boolean = false,
)

enum class InfoBarType {
    SYNC,
    FOLLOW_UP,
    ENROLLMENT_STATUS,
}
