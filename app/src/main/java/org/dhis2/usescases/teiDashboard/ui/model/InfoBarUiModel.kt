package org.dhis2.usescases.teiDashboard.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.hisp.dhis.android.core.enrollment.Enrollment

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
