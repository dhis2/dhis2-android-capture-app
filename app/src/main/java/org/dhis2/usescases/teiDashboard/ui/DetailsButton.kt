package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.R

@Composable
fun DetailsButton(trackedEntityName: String, onButtonClicked: () -> Unit) {
    OutlinedButton(
        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
        shape = RoundedCornerShape(4.dp),
        onClick = onButtonClicked,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_navigation_details),
            contentDescription = "details",
            tint = MaterialTheme.colors.primary
        )
        Text(
            text = stringResource(id = R.string.open_details)
                .format(trackedEntityName)
                .toUpperCase(Locale.current),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(start = 11.dp)
        )
    }
}

fun ComposeView?.setButtonContent(trackedEntityName: String, onButtonClicked: () -> Unit) {
    this?.setContent {
        MdcTheme {
            DetailsButton(
                trackedEntityName = trackedEntityName,
                onButtonClicked = onButtonClicked
            )
        }
    }
}

@Preview
@Composable
fun DetailsButtonPreview() {
    DetailsButton("PERSON") {}
}
