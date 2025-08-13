package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModel
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.action_next
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
const val ServerValidationContentButtonTag = "ServerValidationContentButtonTag"

@Composable
internal fun ServerValidationContent() {
    val viewModel: LoginViewModel = koinViewModel()

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
    ) {
        Button(
            modifier = Modifier.fillMaxWidth()
                .testTag(ServerValidationContentButtonTag),
            enabled = true,
            style = ButtonStyle.FILLED,
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Import database",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            },
            text = stringResource(Res.string.action_next),
            onClick = viewModel::onValidateServer,
        )
    }
}
