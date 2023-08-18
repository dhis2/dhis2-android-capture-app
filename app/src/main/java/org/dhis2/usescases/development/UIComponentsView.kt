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
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButton
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class UIComponentsView : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            DHIS2Theme {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = SurfaceColor.Container
            ) {
                Components()
            }
//            }
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
        Button(text = "Button outlined") {}
        /*Button(text = "Button with icon",
            icon = { dhis2IconResource(resource = SharedRes.images.dhis2_4x4_outline) }) {}*/
        Button(text = "Button elevated", style = ButtonStyle.ELEVATED) {}
        Button(
            text = "Button icon",
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_file),
                    contentDescription = ""
                )
            }
        ) {}
        Button(text = "Button filled", style = ButtonStyle.FILLED) {}
        Button(text = "Button text", style = ButtonStyle.TEXT) {}

//        IconButton(icon = { Icons.Rounded.LocationOn }) {}
//        SquareIconButton(icon = { Icons.Outlined.AccountBox }) {}

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
