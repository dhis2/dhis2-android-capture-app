package org.dhis2.android.rtsm.ui.home.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.utils.Utils.Companion.capitalizeText

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Toolbar(
    title: String,
    subtitle: String,
    themeColor: Color,
    navigationAction: () -> Unit,
    backdropState: BackdropScaffoldState
) {

    val scope = rememberCoroutineScope()

    TopAppBar(
        title = {
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = capitalizeText(title).ifBlank { "Home" },
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    fontSize = 20.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.subtitle2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = 12.sp
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { navigationAction() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_sync),
                        contentDescription = null,
                        tint = colorResource(id = R.color.white)
                    )
                }

                IconButton(
                    onClick = {
                        if (backdropState.isConcealed) {
                            scope.launch { backdropState.reveal() }
                        } else scope.launch { backdropState.conceal() }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_filter),
                        contentDescription = null,
                        tint = colorResource(id = R.color.white)
                    )
                }
            }
        },
        backgroundColor = themeColor,
        contentColor = Color.White,
        elevation = 0.dp,
    )
}