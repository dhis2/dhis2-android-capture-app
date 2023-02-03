package org.dhis2.commons.ui.icons

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.core.graphics.toColorInt
import org.dhis2.commons.R
import org.dhis2.ui.icons.SyncingIcon
import org.hisp.dhis.android.core.common.State

@Composable
fun State.toIconData(): Pair<ImageVector, Color> {
    val imageVector = when (this) {
        State.TO_POST,
        State.TO_UPDATE,
        State.UPLOADING ->
            ImageVector.vectorResource(id = R.drawable.ic_sync_problem_grey)
        State.ERROR -> ImageVector.vectorResource(id = R.drawable.ic_sync_problem_red)
        State.WARNING -> ImageVector.vectorResource(id = R.drawable.ic_sync_warning)
        State.SENT_VIA_SMS,
        State.SYNCED_VIA_SMS -> ImageVector.vectorResource(id = R.drawable.ic_sync_sms)
        else -> ImageVector.vectorResource(id = R.drawable.ic_status_synced)
    }

    val tint = when (this) {
        State.TO_POST,
        State.TO_UPDATE,
        State.UPLOADING -> Color("#333333".toColorInt())
        State.ERROR -> Color("#E91E63".toColorInt())
        State.WARNING -> Color("#FF9800".toColorInt())
        State.SENT_VIA_SMS,
        State.SYNCED_VIA_SMS -> Color("#03A9F4".toColorInt())
        else -> Color("#4CAF50".toColorInt())
    }

    return Pair(imageVector, tint)
}

@Composable
fun SyncStateIcon(state: State) {
    if (state != State.UPLOADING) {
        val (iconResource, tintColor) = state.toIconData()
        Icon(
            imageVector = iconResource,
            tint = tintColor,
            contentDescription = "sync"
        )
    } else {
        SyncingIcon()
    }
}
