package org.dhis2.mobile.login.authentication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.two_fa_code
import org.dhis2.mobile.login.resources.two_fa_disable_button
import org.dhis2.mobile.login.resources.two_fa_disable_desc_1
import org.dhis2.mobile.login.resources.two_fa_disable_desc_2
import org.dhis2.mobile.login.resources.two_fa_disable_error
import org.dhis2.mobile.login.resources.two_fa_disable_title
import org.dhis2.mobile.login.resources.two_fa_disabling_button
import org.dhis2.mobile.login.resources.two_fa_is_on
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.jetbrains.compose.resources.stringResource

@Composable
fun TwoFADisableScreen(
    twoFADisableUiState: TwoFAUiState.Disable,
    onAuthCodeUpdated: (String) -> Unit,
    onDisable: (String) -> Unit,
) {
    var authCode: TextFieldValue by remember(twoFADisableUiState) { mutableStateOf(TextFieldValue("")) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoBar(
            text = stringResource(Res.string.two_fa_is_on),
            textColor = SurfaceColor.CustomGreen,
            backgroundColor = SurfaceColor.CustomGreen.copy(alpha = 0.1f),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    tint = SurfaceColor.CustomGreen,
                    contentDescription = stringResource(Res.string.two_fa_is_on),
                )
            },
        )
        Text(
            text = stringResource(Res.string.two_fa_disable_title),
            color = TextColor.OnSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        TextStep(
            stepNumber = "1.",
            text = stringResource(Res.string.two_fa_disable_desc_1),
        )
        TextStep(
            stepNumber = "2.",
            text = stringResource(Res.string.two_fa_disable_desc_2),
        ) {
            InputText(
                inputTextFieldValue = authCode,
                onValueChanged = {
                    it?.let {
                        authCode = it
                        onAuthCodeUpdated(it.text)
                    }
                },
                title = stringResource(Res.string.two_fa_code),
                state = twoFADisableUiState.state,
                supportingText =
                    if (twoFADisableUiState.state == InputShellState.ERROR) {
                        listOf(
                            SupportingTextData(
                                text = stringResource(Res.string.two_fa_disable_error),
                                state = SupportingTextState.ERROR,
                            ),
                        )
                    } else {
                        null
                    },
            )
            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                text =
                    when (twoFADisableUiState.isDisabling) {
                        true -> stringResource(Res.string.two_fa_disabling_button)
                        else -> stringResource(Res.string.two_fa_disable_button)
                    },
                colorStyle = ColorStyle.ERROR,
                style = ButtonStyle.FILLED,
                enabled = authCode.text.length >= 6 && twoFADisableUiState.isDisabling.not(),
                onClick = { onDisable(authCode.text) },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.KeyOff,
                        contentDescription = stringResource(Res.string.two_fa_disable_button),
                    )
                },
            )
        }
    }
}

@Composable
fun TextStep(
    stepNumber: String,
    text: String,
    bottomContent: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier =
            Modifier
                .background(
                    color = SurfaceColor.ContainerLow,
                    shape = RoundedCornerShape(16.dp),
                ).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stepNumber,
                color = TextColor.OnSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = text,
                color = TextColor.OnSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        bottomContent?.invoke()
    }
}
