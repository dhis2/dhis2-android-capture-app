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
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ServerValidationContent() {
    val viewModel: LoginViewModel = koinViewModel()

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = true,
            style = ButtonStyle.FILLED,
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Import database",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            },
            text = "Next",
            onClick = viewModel::onValidateServer,
        )
    }
}
