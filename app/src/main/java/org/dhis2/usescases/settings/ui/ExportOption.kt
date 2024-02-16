package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.ui.IconTextButton

@Composable
fun ExportOption(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        IconTextButton(
            onClick = onClick,
            painter = rememberVectorPainter(image = Icons.Filled.Share),
            text = stringResource(
                id = R.string.share
            )
        )
    }
}

fun ComposeView.setExportOption(
    onClick: () -> Unit
) {
    setContent {
        ExportOption(onClick)
    }
}