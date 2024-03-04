package org.dhis2.usescases.settings.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.R
import org.dhis2.ui.dialogs.alert.Dhis2AlertDialogUi
import org.dhis2.ui.model.ButtonUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType

@Composable
fun ExportOption(
    onDownload: () -> Unit,
    onShare: () -> Unit,
    displayProgress: Boolean,
) {
    val context = LocalContext.current

    var onPermissionGrantedCallback: () -> Unit = {}
    var showPermissionDialog by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            onPermissionGrantedCallback()
        } else {
            showPermissionDialog = true
        }
    }

    val permissionSettingLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onPermissionGrantedCallback()
            }
            onPermissionGrantedCallback = {}
        }

    AnimatedContent(
        targetState = displayProgress,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(700),
            ) togetherWith fadeOut(animationSpec = tween(700))
        },
        label = "import content",
    ) { targetState ->

        if (targetState.not()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(
                        start = 72.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (displayProgress) Arrangement.Center else spacedBy(16.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            onDownload()
                        } else {
                            onPermissionGrantedCallback = onDownload
                            launcher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    },
                    style = ButtonStyle.TEXT,
                    text = stringResource(id = R.string.download),
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_file_download),
                            contentDescription = "Download",
                            tint = MaterialTheme.colors.primary,
                        )
                    },
                )

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            onShare()
                        } else {
                            onPermissionGrantedCallback = onShare
                            launcher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    },
                    style = ButtonStyle.TEXT,
                    text = stringResource(id = R.string.share),
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colors.primary,
                        )
                    },
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (displayProgress) Arrangement.Center else spacedBy(16.dp),
            ) {
                ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
            }
        }
    }

    if (showPermissionDialog) {
        Dhis2AlertDialogUi(
            labelText = stringResource(id = R.string.permission_denied),
            descriptionText = "You need to provide the permission to carry out this action",
            iconResource = R.drawable.ic_info,
            dismissButton = ButtonUiModel("Cancel") {
                showPermissionDialog = false
                onPermissionGrantedCallback = {}
            },
            confirmButton = ButtonUiModel("Change permission") {
                permissionSettingLauncher.launch(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    ),
                )
            },
        )
    }
}

@Preview
@Composable
fun PreviewExportOption() {
    Mdc3Theme {
        ExportOption(onDownload = { }, onShare = { }, false)
    }
}

@Preview
@Composable
fun PreviewExportOptionProgress() {
    Mdc3Theme {
        ExportOption(onDownload = { }, onShare = { }, true)
    }
}

fun ComposeView.setExportOption(
    onDownload: () -> Unit,
    onShare: () -> Unit,
    displayProgressProvider: () -> LiveData<Boolean>,
) {
    setContent {
        val displayProgress by displayProgressProvider().observeAsState(false)
        MdcTheme {
            ExportOption(onShare, onDownload, displayProgress)
        }
    }
}
