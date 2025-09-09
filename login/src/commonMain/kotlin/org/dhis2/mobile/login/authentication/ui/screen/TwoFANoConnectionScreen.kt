package org.dhis2.mobile.login.authentication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SignalWifiConnectedNoInternet4
import androidx.compose.material.icons.outlined.WifiFind
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.two_fa_no_connection_button
import org.dhis2.mobile.login.resources.two_fa_no_connection_description
import org.dhis2.mobile.login.resources.two_fa_no_connection_title
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.jetbrains.compose.resources.stringResource

@Composable
fun TwoFANoConnectionScreen(onRetry: () -> Unit = {}) {
    Column {
        InfoBar(
            text = stringResource(Res.string.two_fa_no_connection_title),
            textColor = TextColor.OnErrorContainer,
            backgroundColor = SurfaceColor.ErrorContainer,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SignalWifiConnectedNoInternet4,
                    contentDescription = stringResource(Res.string.two_fa_no_connection_title),
                    tint = TextColor.OnErrorContainer,
                )
            },
        )

        Column(
            modifier =
                Modifier
                    .padding(top = 16.dp)
                    .background(
                        color = SurfaceColor.ContainerLow,
                        shape = RoundedCornerShape(Radius.M),
                    ).padding(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.two_fa_no_connection_description),
                style = MaterialTheme.typography.bodyLarge,
            )

            Button(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                style = ButtonStyle.FILLED,
                text = stringResource(Res.string.two_fa_no_connection_button),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.WifiFind,
                        contentDescription = "Status Icon",
                    )
                },
                onClick = onRetry,
            )
        }
    }
}
