package org.dhis2.mobile.commons.auth

import android.app.Activity.RESULT_OK
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.openid_login_cancelled
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.jetbrains.compose.resources.getString

class OpenIdControllerImpl(
    context: FragmentActivity,
) : OpenIdController {
    private var openIDRequestCode = -1
    private lateinit var onResultCallback: (suspend (Result<Any>) -> Unit)

    private val openIdLauncher =
        context.activityResultRegistry.register(
            key = "OPEN_ID_LAUNCHER",
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let {
                    val intentResult =
                        IntentWithRequestCode(
                            intent = it,
                            requestCode = openIDRequestCode,
                        )
                    context.lifecycleScope.launch {
                        onResultCallback(Result.success(intentResult))
                    }
                }
            } else {
                context.lifecycleScope.launch {
                    onResultCallback(
                        Result.failure<IntentWithRequestCode>(Exception(getString(Res.string.openid_login_cancelled))),
                    )
                }
            }
        }

    override fun handleIntent(
        intent: Any,
        onResult: suspend (Result<Any>) -> Unit,
    ) {
        if (intent !is IntentWithRequestCode) return
        onResultCallback = onResult
        openIDRequestCode = intent.requestCode
        openIdLauncher.launch(intent.intent)
    }
}
