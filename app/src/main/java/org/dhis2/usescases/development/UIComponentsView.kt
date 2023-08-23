package org.dhis2.usescases.development

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.ui.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.InputShell
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButton
import org.hisp.dhis.mobile.ui.designsystem.component.SimpleTextInputField
import org.hisp.dhis.mobile.ui.designsystem.component.SquareIconButton
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon
import org.hisp.dhis.mobile.ui.designsystem.resource.provideStringResource
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class UIComponentsView : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DHIS2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceColor.Container
                ) {
                    Components()
                }
            }
        }
    }
}

@Composable
fun Components() {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)

        InputShell(
            title = "input shel",
            primaryButton = {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = ""
                )
            },
            secondaryButton = {
                Icon(
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = ""
                )
            },
            inputField = {
                SimpleTextInputField()
            }
        ) {

        }
        Button(
            text = provideStringResource("show_more"),
            icon = {
                Icon(
                    painter = provideDHIS2Icon(resourceName = "dhis2_blood_a_n_positive"),
                    contentDescription = ""
                )
            },
            style = ButtonStyle.KEYBOARDKEY
        ) {}
        Button(
            text = "Button app icon",
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_file),
                    contentDescription = ""
                )
            },
            style = ButtonStyle.ELEVATED
        ) {}
        Button(
            text = "Button material icon",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = ""
                )
            },
            style = ButtonStyle.FILLED
        ) {}

        IconButton(icon = {
            Icon(
                imageVector = Icons.Outlined.AccountBox,
                contentDescription = ""
            )
        }) {}
        SquareIconButton(icon = {
            Icon(
                imageVector = Icons.Outlined.AccountBox,
                contentDescription = ""
            )
        }) {}

        RadioButton(
            selected = true,
            enabled = true,
            textInput = "hola"
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Components()
}
