package org.dhis2.android.rtsm.ui.home.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainContent(dpHeight: Dp, backdropState: BackdropScaffoldState, toolbartitle: String) {
    val scope = rememberCoroutineScope()
    val resource = painterResource(R.drawable.ic_arrow_up)
    val commentsAlpha = if (backdropState.isRevealed) 1f else 0f
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        IconButton(
            onClick = {
                scope.launch { backdropState.conceal() }
            },
            modifier =Modifier
                .alpha(commentsAlpha)
                .padding(8.dp)
        ) {
            Icon(resource, contentDescription = null, tint = MaterialTheme.colors.primary)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$dpHeight \n Title $toolbartitle")
    }
}