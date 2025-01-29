package org.dhis2.form.ui.dialog

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.dhis2.form.R
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonCarousel
import org.hisp.dhis.mobile.ui.designsystem.component.CarouselButtonData
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
internal fun ImagePickerOptionsDialog(
    title: String,
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
            title = title,
            icon = {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = SurfaceColor.Primary)
            },
            onDismiss = onDismiss,
            buttonBlock = {
                val context = LocalContext.current
                ButtonCarousel(
                    carouselButtonList = listOf(
                        CarouselButtonData(
                            onClick = {
                                onDismiss()
                                onTakePicture(context)
                            },
                            enabled = true,
                            text = stringResource(R.string.take_photo),
                            icon = {
                                Icon(
                                    Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = TextColor.OnSurface,
                                )
                            },
                        ),
                        CarouselButtonData(
                            onClick = {
                                onDismiss()
                                onSelectFromGallery()
                            },
                            enabled = true,
                            text = stringResource(R.string.from_gallery_v2),
                            icon = {
                                Icon(
                                    Icons.Outlined.Collections,
                                    contentDescription = null,
                                    tint = TextColor.OnSurface,
                                )
                            },
                        ),
                    ),
                )
            },
            content = null,
        )
    }
}
