package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@ExperimentalAnimationApi
@Composable
fun ReopenButton(
    visible: Boolean,
    onReopenClickListener: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Button(
            onClick = onReopenClickListener,
            shape = RoundedCornerShape(24.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.section_warning_color),
                ),
            contentPadding = PaddingValues(10.dp),
            modifier =
                Modifier
                    .height(40.dp)
                    .wrapContentWidth(),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock_open_white),
                contentDescription = "reopen",
                tint = Color.White,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Re-open form",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@ExperimentalAnimationApi
@Preview
@Composable
fun ReopenButtonPreview() {
    DHIS2Theme {
        ReopenButton(visible = true) {}
    }
}
