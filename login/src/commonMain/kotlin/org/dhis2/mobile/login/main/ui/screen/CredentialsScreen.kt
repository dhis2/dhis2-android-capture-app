package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.dhis2.mobile.login.main.ui.components.TaskExecutorButton
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyleData
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InputPassword
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputUser
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.model.InputPasswordModel
import org.hisp.dhis.mobile.ui.designsystem.component.model.InputUserModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

@Composable
fun CredentialsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = spacedBy(Spacing.Spacing24)
    ) {
        ServerInfo(
            serverName = "ServerName",
            serverUrl = "https://serverurl.net/dhis2",
            serverImageUrl = "https://en.wikipedia.org/wiki/Flag_of_Spain#/media/File:Flag_of_Spain.svg",
        )
        CredentialsContainer(
            availableUsernames = emptyList(),
            username = "",
            password = "",
        )
        LoginStatus(
            isLoggingIn = false,
            loginErrorMessage = null,
            onCancelLogin = {

            }
        )
//        CredentialActions()
//        ManageAccountsButton()
    }
}

@Composable
private fun ServerInfo(
    serverName: String,
    serverUrl: String,
    serverImageUrl: String?,
) {
    ListCard(
        modifier = Modifier.fillMaxWidth(),
        listCardState = rememberListCardState(
            title = ListCardTitleModel(
                text = serverName
            ),
            description = ListCardDescriptionModel(
                text = serverUrl
            ),
            additionalInfoColumnState = rememberAdditionalInfoColumnState(
                additionalInfoList = emptyList(),
                syncProgressItem = AdditionalInfoItem(
                    value = ""
                )
            ),
        ),
        listAvatar = {
            if (serverImageUrl != null) {
                AsyncImage(
                    model = serverImageUrl,
                    contentDescription = "",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Avatar(
                    style = AvatarStyleData.Text(serverName.take(1))
                )
            }
        },
        onCardClick = { },
    )
}

@Composable
private fun CredentialsContainer(
    username: String,
    password: String,
    availableUsernames: List<String>,
) {
    var usernameTextValue by remember(username) {
        mutableStateOf(
            TextFieldValue(username)
        )
    }

    var passwordTextValue by remember(password) {
        mutableStateOf(
            TextFieldValue(password)
        )
    }

    val areCredentialsComplete by remember(username, password) {
        derivedStateOf {
            username.isNotBlank() && password.isNotBlank()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = spacedBy(Spacing.Spacing8)
    ) {
        InputUser(
            modifier = Modifier.fillMaxWidth(),
            uiModel = InputUserModel(
                title = "Username",
                state = InputShellState.UNFOCUSED,
                inputTextFieldValue = usernameTextValue,
                autoCompleteList = availableUsernames,
                autoCompleteItemSelected = {
                    usernameTextValue =
                        it?.let { text -> TextFieldValue(text) } ?: TextFieldValue("")

                },
                onNextClicked = {

                },
                onValueChanged = {
                    usernameTextValue = it ?: TextFieldValue("")
                },
                onFocusChanged = {

                },
                imeAction = if (areCredentialsComplete) ImeAction.Done else ImeAction.Next,
            ),
        )
        InputPassword(
            modifier = Modifier.fillMaxWidth(),
            uiModel = InputPasswordModel(
                title = "Password",
                state = InputShellState.UNFOCUSED,
                inputTextFieldValue = passwordTextValue,
                onNextClicked = {},
                onValueChanged = {
                    passwordTextValue = it ?: TextFieldValue("")
                },
                onFocusChanged = {},
                imeAction = if (areCredentialsComplete) ImeAction.Done else ImeAction.Next,
            )
        )
    }
}

@Composable
private fun LoginStatus(
    isLoggingIn: Boolean,
    loginErrorMessage: String?,
    onCancelLogin: () -> Unit,
) {
    if (isLoggingIn) {
        TaskExecutorButton(
            modifier = Modifier,
            enabled = true,
            taskRunning = true,
            actionText = "",
            taskRunningText = "Logging in...",
            icon = {},
            onClick = {},
            onCancel = onCancelLogin
        )
    } else if (loginErrorMessage != null) {
        InfoBar(
            modifier = Modifier,
            text = loginErrorMessage,
            textColor = MaterialTheme.colorScheme.onErrorContainer,
            actionText = "",
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = ""
                )
            },
            onActionClick = {},
        )
    }
}
