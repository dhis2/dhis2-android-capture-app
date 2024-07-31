package org.dhis2.mobile.myplugin

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.runtime.Composable
import org.dhis2.commons.plugin.PluginInterface
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.CardDetail

class PluginImpl : PluginInterface {
    @Composable
    override fun Show() {
        MainScreen()
    }

    @Composable
    fun MainScreen() {
        Column {
            CardDetail(
                title = "This is my amazing plugin!!!",
                additionalInfoList = listOf(
                    AdditionalInfoItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Android,
                                contentDescription = "This is my amazing plugin!!!",
                                tint = AdditionalInfoItemColor.SUCCESS.color,
                            )
                        },
                        value = "Lets Rock",
                        color = AdditionalInfoItemColor.ERROR.color,
                        isConstantItem = true,
                    )
                )
            )
        }
    }
}