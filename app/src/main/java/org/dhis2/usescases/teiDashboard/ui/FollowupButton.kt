package org.dhis2.usescases.teiDashboard.ui

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
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.R

@Composable
fun FollowupButton(
        trackedEntityName: String,
        followUp: Boolean,
        onButtonClicked: () -> Unit,

) {
    OutlinedButton(
            border = BorderStroke(1.dp, MaterialTheme.colors.primary),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(0.dp),
            onClick = onButtonClicked,
            modifier= Modifier
                .size(height = 72.dp, width = 40.dp)
                .padding(vertical = 16.dp)

    ) {

        trackedEntityName.replaceFirstChar { it.uppercase() };
        println("Follow-Up on Kotlin::: " + followUp)
//        println(followUp)
        if (followUp) {
            Icon(
                painter = painterResource(id = R.drawable.ic_follow_up_outlined_warn),
//                painter = painterResource(id = R.drawable.ic_follow_up),
                contentDescription = "details",
                tint = MaterialTheme.colors.error
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_follow_up_outlined),
//                painter = painterResource(id = R.drawable.ic_follow_up),
                contentDescription = "details",
                tint = MaterialTheme.colors.primary
            )
        }

//        Text(
//                text = stringResource(id = R.string.open_details)
//                        .format(trackedEntityName),
//                fontSize = 12.sp,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colors.primary,
//                modifier = Modifier.padding(start = 11.dp)
//        )
    }
}

fun ComposeView?.setFollowupButtonContent(
        trackedEntityName: String,
        followUp: Boolean,
        onButtonClicked: () -> Unit,

) {
    this?.setContent {
        MdcTheme {
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
        false
    ) {}
}
