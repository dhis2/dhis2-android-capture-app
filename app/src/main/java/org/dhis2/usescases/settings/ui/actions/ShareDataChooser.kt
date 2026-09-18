package org.dhis2.usescases.settings.ui.actions

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.dhis2.R

@Composable
fun ShareDataChooser(data: String) {
    val context = LocalContext.current
    val subject = stringResource(R.string.sync_error_title)
    val chooserTitle = stringResource(R.string.share_with)

    LaunchedEffect(data) {
        val sendIntent =
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, data)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                type = "text/plain"
            }
        context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
    }
}
