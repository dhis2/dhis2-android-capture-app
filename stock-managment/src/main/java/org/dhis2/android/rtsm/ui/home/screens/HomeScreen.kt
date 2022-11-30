package org.dhis2.android.rtsm.ui.home.screens

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.home.HomeActivity
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.screens.components.Backdrop
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    activity: Activity,
    viewModel: HomeViewModel = viewModel(),
    manageStockViewModel: ManageStockViewModel = viewModel(),
    themeColor: Color,
    supportFragmentManager: FragmentManager,
    homeContext: HomeActivity,
    barcodeLauncher: ActivityResultLauncher<ScanOptions>,
    proceedAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> }
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            AnimatedVisibility(
                visible = checkVisibility(viewModel = viewModel),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CompositionLocalProvider(
                    LocalRippleTheme provides
                        if (enabled) LocalRippleTheme.current
                        else NoRippleTheme
                ) {
                    ExtendedFloatingActionButton(
                        modifier = Modifier
                            .shadow(
                                ambientColor = Color.Black.copy(alpha = 0.5f),
                                clip = false,
                                elevation = 0.dp
                            )
                            .height(70.dp)
                            .animateEnterExit(
                                enter = fadeIn(), exit = fadeOut()
                            ),
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.proceed_icon),
                                contentDescription = stringResource(R.string.review),
                                tint = if (enabled) themeColor
                                else colorResource(id = R.color.proceed_text_color)
                            )
                        },
                        text = {
                            Text(
                                stringResource(R.string.review),
                                color = if (enabled) themeColor
                                else colorResource(id = R.color.proceed_text_color)
                            )
                        },
                        onClick = {
                            if (enabled) {
                                enabled = !enabled
                                proceedAction(scope, scaffoldState)
                            } else {
                                enabled = !enabled
                            }
                        },
                        backgroundColor = if (enabled) Color.White
                        else colorResource(id = R.color.proceed_color),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = it) { data ->
                Snackbar(
                    snackbarData = data, backgroundColor = colorResource(R.color.error)
                )
            }
        }
    ) {
        it.calculateBottomPadding()
        Backdrop(
            activity,
            viewModel,
            manageStockViewModel,
            themeColor,
            supportFragmentManager,
            homeContext,
            barcodeLauncher,
            scaffoldState
        ) { coroutineScope, scaffold ->
            syncAction(coroutineScope, scaffold)
        }
    }
}
@Composable
fun checkVisibility(viewModel: HomeViewModel): Boolean {
    return if ((
        viewModel.toolbarTitle.collectAsState().value.name ==
            TransactionType.DISCARD.name
        )
    ) {
        return viewModel.hasFacilitySelected.collectAsState().value
    } else if ((
        viewModel.toolbarTitle.collectAsState().value.name ==
            TransactionType.CORRECTION.name
        )
    ) {
        return viewModel.hasFacilitySelected.collectAsState().value
    } else (
        (
            viewModel.toolbarTitle.collectAsState().value.name ==
                TransactionType.DISTRIBUTION.name
            ) &&
            viewModel.hasFacilitySelected.collectAsState().value &&
            viewModel.hasDestinationSelected.collectAsState().value
        )
}
private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        RippleAlpha(
            0.0f, 0.0f,
            0.0f, 0.0f
        )
}
