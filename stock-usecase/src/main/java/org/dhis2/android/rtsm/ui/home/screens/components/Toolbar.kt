package org.dhis2.android.rtsm.ui.home.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.utils.Utils.Companion.capitalizeText
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Toolbar(
    title: String,
    from: String,
    to: String?,
    themeColor: Color,
    launchBottomSheet: () -> Unit,
    backdropState: BackdropScaffoldState,
    scaffoldState: ScaffoldState,
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
    hasFacilitySelected: Boolean,
    hasDestinationSelected: Boolean?,
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    TopAppBar(
        title = {
            Column(
                modifier = Modifier.offset(x = (-16).dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text =
                        capitalizeText(title).ifBlank {
                            stringResource(R.string.title_activity_home)
                        },
                    color = SurfaceColor.SurfaceBright,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = from,
                        style = MaterialTheme.typography.titleSmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontSize = 12.sp,
                        color = colorResource(R.color.toolbar_subtitle),
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    this@Column.ProvideToolBarIcons(to, hasFacilitySelected, hasDestinationSelected, title = title)
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    launchBottomSheet.invoke()
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                },
            )
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    onClick = {
                        syncAction(scope, scaffoldState)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_sync),
                            contentDescription = null,
                            tint = colorResource(id = R.color.white),
                        )
                    },
                )

                IconButton(
                    onClick = {
                        if (backdropState.isConcealed) {
                            scope.launch { backdropState.reveal() }
                        } else {
                            scope.launch { backdropState.conceal() }
                        }
                        keyboardController?.hide()
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_filter),
                            contentDescription = null,
                            tint = colorResource(id = R.color.white),
                        )
                    },
                )
            }
        },
        backgroundColor = themeColor,
        contentColor = Color.White,
        elevation = 0.dp,
    )
}

@Composable
fun ColumnScope.ProvideToolBarIcons(
    to: String?,
    hasFacilitySelected: Boolean,
    hasDestinationSelected: Boolean?,
    title: String,
) {
    if (to != null) {
        Icon(
            painter = painterResource(id = R.drawable.ic_from_to),
            contentDescription = null,
            Modifier
                .padding(start = 5.dp, end = 5.dp)
                .size(18.dp),
            tint = colorResource(R.color.toolbar_subtitle),
        )
        Text(
            text = to,
            style = MaterialTheme.typography.titleSmall,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            fontSize = 12.sp,
            color = colorResource(R.color.toolbar_subtitle),
            modifier = Modifier.weight(1f, fill = false),
        )
    }

    if (!hasFacilitySelected) {
        AlertIcon()
    } else if (TransactionType.DISTRIBUTION.name.equals(title, true)) {
        hasDestinationSelected?.let {
            if (!it) {
                AlertIcon()
            }
        }
    }
}

@Composable
fun AnalyticsTopBar(
    title: String,
    themeColor: Color,
    backAction: () -> Unit,
    scaffoldState: ScaffoldState,
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
) {
    val scope = rememberCoroutineScope()
    TopAppBar(
        title = {
            Column(
                modifier = Modifier.offset(x = (-16).dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text =
                        capitalizeText(title).ifBlank {
                            stringResource(R.string.title_activity_home)
                        },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    backAction.invoke()
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                },
            )
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    onClick = {
                        syncAction(scope, scaffoldState)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_sync),
                            contentDescription = null,
                            tint = colorResource(id = R.color.white),
                        )
                    },
                )
            }
        },
        backgroundColor = themeColor,
        contentColor = Color.White,
        elevation = 0.dp,
    )
}

@Composable
private fun AlertIcon() {
    Icon(
        painter = painterResource(id = R.drawable.ic_alert),
        contentDescription = null,
        Modifier
            .size(18.dp)
            .padding(start = 5.dp),
        tint = Color.White,
    )
}
