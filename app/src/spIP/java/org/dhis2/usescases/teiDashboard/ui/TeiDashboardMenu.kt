package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sms
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.EnrollmentMenuItem
import org.dhis2.usescases.teiDashboard.EnrollmentMenuItem.SEND_SMS
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuLeadingElement



fun MutableList<MenuItemData<EnrollmentMenuItem>>.addSendSmsMenuItem(resourceManager: ResourceManager) {
    add(
        MenuItemData(
            id = SEND_SMS,
            label = resourceManager.getString(R.string.send_sms),
            leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Sms),
        ),
    )
}
