package org.dhis2.usescases.teidashboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import org.dhis2.R

@Composable
fun FollowupButton(
    trackedEntityName: String,
    followUp: Boolean,
    onButtonClicked: () -> Unit,

) {
    if (followUp) {
        OutlinedButton(
            border = BorderStroke(1.dp, MaterialTheme.colors.error),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(0.dp),
            onClick = onButtonClicked,
            modifier = Modifier
                .size(height = 72.dp, width = 40.dp)
                .padding(vertical = 16.dp),

        ) {
            trackedEntityName.replaceFirstChar { it.uppercase() }
            Icon(
                painter = painterResource(id = R.drawable.ic_follow_up_outlined_warn),
                contentDescription = "details",
                tint = MaterialTheme.colors.error,
            )
        }
    } else {
        OutlinedButton(
            border = BorderStroke(1.dp, MaterialTheme.colors.primary),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(0.dp),
            onClick = onButtonClicked,
            modifier = Modifier
                .size(height = 72.dp, width = 40.dp)
                .padding(vertical = 16.dp),

        ) {
            trackedEntityName.replaceFirstChar { it.uppercase() }
            Icon(
                painter = painterResource(id = R.drawable.ic_follow_up_outlined),
                contentDescription = "details",
                tint = MaterialTheme.colors.primary,
            )
        }
    }
}

fun ComposeView?.setFollowupButtonContent(
    trackedEntityName: String,
    followUp: Boolean,
    onButtonClicked: () -> Unit,

) {
    this?.setContent {
        Mdc3Theme {
            FollowupButton(
                trackedEntityName = trackedEntityName,
                followUp = followUp,
                onButtonClicked = onButtonClicked,

            )
        }
    }
}

@Preview
@Composable
fun FollowupButtonPreview() {
    FollowupButton(
        "PERSON",
        false,
    ) {}
}
