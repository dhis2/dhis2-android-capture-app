package org.dhis2.mobile.login.authentication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.KeyOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.play_store
import org.dhis2.mobile.login.resources.two_fa_authentication_code
import org.dhis2.mobile.login.resources.two_fa_failed_to_turn_on
import org.dhis2.mobile.login.resources.two_fa_no_connection_title
import org.dhis2.mobile.login.resources.two_fa_status_off
import org.dhis2.mobile.login.resources.two_fa_to_enable_google_authenticator
import org.dhis2.mobile.login.resources.two_fa_to_enable_step_one_description
import org.dhis2.mobile.login.resources.two_fa_to_enable_step_one_number
import org.dhis2.mobile.login.resources.two_fa_to_enable_step_three_description
import org.dhis2.mobile.login.resources.two_fa_to_enable_step_three_number
import org.dhis2.mobile.login.resources.two_fa_to_enable_step_two_description
import org.dhis2.mobile.login.resources.two_fa_to_enable_step_two_number
import org.dhis2.mobile.login.resources.two_fa_turn_on_button
import org.dhis2.mobile.login.resources.two_fa_turn_on_title
import org.dhis2.mobile.login.resources.two_fa_turning_on_button
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

const val TURN_ON_BUTTON_TEST_TAG = "turn_on_button_test_tag"

@Composable
fun TwoFAToEnableScreen(
    enableUiState: TwoFAUiState.Enable,
    onAuthenticatorButtonClicked: () -> Unit,
    onCopyCodeButtonClicked: (String) -> Unit,
    onEnableButtonClicked: (String) -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(Spacing.Spacing12),
    ) {
        InfoBar(
            text = stringResource(Res.string.two_fa_status_off),
            textColor = TextColor.OnWarningContainer,
            backgroundColor = SurfaceColor.WarningContainer,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.KeyOff,
                    contentDescription = stringResource(Res.string.two_fa_no_connection_title),
                    tint = TextColor.OnWarningContainer,
                )
            },
        )

        Text(
            text = stringResource(Res.string.two_fa_turn_on_title),
            style = MaterialTheme.typography.titleMedium,
        )

        TwoFAAuthStepOne(onAuthenticatorButtonClicked)

        TwoFAAuthStepTwo(enableUiState.secretCode, onCopyCodeButtonClicked)

        TwoFAAuthStepThree(enableUiState, onEnableButtonClicked)
    }
}

@Composable
fun TwoFAAuthStepOne(onAuthenticatorButtonClicked: () -> Unit) {
    Column(
        modifier =
            Modifier
                .background(
                    color = SurfaceColor.ContainerLow,
                    shape = RoundedCornerShape(Radius.M),
                ).padding(16.dp),
    ) {
        Row {
            Text(
                text = stringResource(Res.string.two_fa_to_enable_step_one_number),
                style = MaterialTheme.typography.bodyLarge,
            )

            Column {
                Text(
                    text = stringResource(Res.string.two_fa_to_enable_step_one_description),
                    style = MaterialTheme.typography.bodyLarge,
                )

                Button(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    style = ButtonStyle.ELEVATED,
                    text = stringResource(Res.string.two_fa_to_enable_google_authenticator),
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.play_store),
                            contentDescription = stringResource(Res.string.two_fa_to_enable_google_authenticator),
                            tint = Color.Unspecified,
                        )
                    },
                    onClick = onAuthenticatorButtonClicked,
                )
            }
        }
    }
}

@Composable
fun TwoFAAuthStepTwo(
    secretCode: String,
    onCopyCodeButtonClicked: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .background(
                    color = SurfaceColor.ContainerLow,
                    shape = RoundedCornerShape(Radius.M),
                ).padding(16.dp),
    ) {
        Row {
            Text(
                text = stringResource(Res.string.two_fa_to_enable_step_two_number),
                style = MaterialTheme.typography.bodyLarge,
            )

            Column {
                Text(
                    text = stringResource(Res.string.two_fa_to_enable_step_two_description),
                    style = MaterialTheme.typography.bodyLarge,
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = CenterVertically,
                ) {
                    SelectionContainer(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = secretCode,
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    color = TextColor.OnPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp,
                                    letterSpacing = 0.15.sp,
                                ),
                        )
                    }

                    IconButton(
                        style = IconButtonStyle.STANDARD,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Status Icon",
                                tint = SurfaceColor.Primary,
                            )
                        },
                        onClick = {
                            onCopyCodeButtonClicked(secretCode)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun TwoFAAuthStepThree(
    enableUiState: TwoFAUiState.Enable,
    onEnableButtonClicked: (String) -> Unit,
) {
    var textValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    Column(
        modifier =
            Modifier
                .background(
                    color = SurfaceColor.ContainerLow,
                    shape = RoundedCornerShape(Radius.M),
                ).padding(16.dp),
        verticalArrangement = spacedBy(Spacing.Spacing12),
    ) {
        Row {
            Text(
                text = stringResource(Res.string.two_fa_to_enable_step_three_number),
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = stringResource(Res.string.two_fa_to_enable_step_three_description),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            verticalArrangement = spacedBy(12.dp),
        ) {
            InputText(
                title = stringResource(Res.string.two_fa_authentication_code),
                supportingText =
                    enableUiState.enableErrorMessage?.let {
                        listOf(
                            SupportingTextData(
                                text = stringResource(Res.string.two_fa_failed_to_turn_on),
                                state = SupportingTextState.ERROR,
                            ),
                        )
                    },
                inputTextFieldValue = textValue,
                onValueChanged = {
                    if (it != null) {
                        textValue = it
                    }
                },
                state = InputShellState.FOCUSED,
            )

            Button(
                enabled =
                    when (enableUiState.isEnabling) {
                        true -> false
                        else -> {
                            textValue.text.length == 6
                        }
                    },
                modifier =
                    Modifier
                        .testTag(TURN_ON_BUTTON_TEST_TAG)
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                style = ButtonStyle.FILLED,
                text =
                    when (enableUiState.isEnabling) {
                        true -> {
                            stringResource(Res.string.two_fa_turning_on_button)
                        }

                        else -> {
                            stringResource(Res.string.two_fa_turn_on_button)
                        }
                    },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Key,
                        contentDescription = "Status Icon",
                    )
                },
                onClick = { onEnableButtonClicked(textValue.text) },
            )
        }
    }
}

@Preview(
    name = "Two Factor enable screen",
    group = "Two Factor authentication",
    showBackground = true,
)
@Composable
fun TwoFAToEnableScreenPreview() {
    DHIS2Theme {
        TwoFAToEnableScreen(
            enableUiState = TwoFAUiState.Enable("SECRETCODE", isEnabling = false),
            onAuthenticatorButtonClicked = {},
            onCopyCodeButtonClicked = {},
            onEnableButtonClicked = {},
        )
    }
}
