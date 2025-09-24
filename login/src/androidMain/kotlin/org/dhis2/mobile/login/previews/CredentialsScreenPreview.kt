package org.dhis2.mobile.login.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.login.main.ui.screen.CredentialsScreen

@Preview
@Composable
fun CredentialsScreenPreview() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
    ) {
        CredentialsScreen(
            selectedServer = "https:pdsafijasodifh.com",
            selectedUsername = "juanito",
            selectedServerName = "Server name",
            allowRecovery = true,
        )
    }
}
