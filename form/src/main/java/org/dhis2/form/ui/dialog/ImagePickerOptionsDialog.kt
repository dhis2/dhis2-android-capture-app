package org.dhis2.form.ui.dialog

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.dhis2.form.R
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

@Composable
internal fun ImagePickerOptionsDialog(
    showImageOptions: Boolean,
    onDismiss: () -> Unit,
    onTakePicture: (Context) -> Unit,
    onSelectFromGallery: () -> Unit,
) {
    AnimatedVisibility(
        visible = showImageOptions,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
    ) {
        BottomSheetShell(
            title = stringResource(R.string.select_option),
            onDismiss = onDismiss,
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = Spacing.Spacing16,
                            vertical = Spacing.Spacing24,
                        ),
                    verticalArrangement = spacedBy(Spacing.Spacing16),
                ) {
                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onTakePicture(context)
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(stringResource(R.string.take_photo))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onSelectFromGallery()
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(stringResource(R.string.from_gallery))
                    }
                }
            },
        )
    }
}
