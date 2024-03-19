package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle

@ExperimentalAnimationApi
@Composable
fun ReopenButton(visible: Boolean, onReopenClickListener: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Button(
            text = stringResource(id = R.string.re_open),
            onClick = onReopenClickListener,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock_open_white),
                    contentDescription = "reopen",
                )
            },
            colorStyle = ColorStyle.ERROR,
            modifier = Modifier
                .height(40.dp)
                .wrapContentWidth(),
        )
    }
}

@ExperimentalAnimationApi
@Preview
@Composable
fun ReopenButtonPreview() {
    ReopenButton(visible = true) {}
}
