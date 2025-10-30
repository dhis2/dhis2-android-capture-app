package org.dhis2.mobile.commons.ui

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
import androidx.compose.ui.Modifier
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonCarousel
import org.hisp.dhis.mobile.ui.designsystem.component.CarouselButtonData
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun ImagePickerOptionsDialog(
    title: String,
    cameraButtonLabel: String,
    galleryButtonLabel: String,
    showImageOptions: Boolean,
    onDismiss: () -> Unit,
    onTakePicture: () -> Unit,
    onSelectFromGallery: () -> Unit,
) {
    val state =
        BottomSheetShellUIState(
            title = title,
        )

    AnimatedVisibility(
        visible = showImageOptions,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
    ) {
        BottomSheetShell(
            uiState = state,
            modifier = Modifier,
            icon = {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = SurfaceColor.Primary)
            },
            onDismiss = onDismiss,
            content = null,
            buttonBlock = {
                ButtonCarousel(
                    carouselButtonList =
                        listOf(
                            CarouselButtonData(
                                onClick = {
                                    onDismiss()
                                    onTakePicture()
                                },
                                enabled = true,
                                text = cameraButtonLabel,
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
                                text = galleryButtonLabel,
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
        )
    }
}
