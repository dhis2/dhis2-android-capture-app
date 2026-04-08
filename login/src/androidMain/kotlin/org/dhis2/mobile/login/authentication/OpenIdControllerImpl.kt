package org.dhis2.mobile.login.authentication

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.openid_error_controller_not_bounded
import org.dhis2.mobile.login.resources.openid_error_expected_intent
import org.dhis2.mobile.login.resources.openid_login_cancelled
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.jetbrains.compose.resources.getString

class OpenIdControllerImpl : OpenIdController {
    private var openIDRequestCode = -1
    private var onResultCallback: (suspend (Result<Any>) -> Unit)? = null
    private var openIdLauncher: ActivityResultLauncher<Intent>? = null
    private var lifecycleScope: LifecycleCoroutineScope? = null

    override fun bind(context: Any) {
        (context as? FragmentActivity)?.let { activity ->
            openIdLauncher?.unregister()
            lifecycleScope = activity.lifecycleScope
            openIdLauncher =
                activity.activityResultRegistry.register(
                    key = "OPEN_ID_LAUNCHER",
                    contract = ActivityResultContracts.StartActivityForResult(),
                ) { result ->
                    val scope = lifecycleScope ?: return@register
                    if (result.resultCode == RESULT_OK && result.data != null) {
                        val intentResult =
                            IntentWithRequestCode(
                                intent = result.data!!,
                                requestCode = openIDRequestCode,
                            )
                        scope.launch {
                            onResultCallback?.invoke(Result.success(intentResult))
                        }
                    } else {
                        scope.launch {
                            onResultCallback?.invoke(
                                Result.failure<IntentWithRequestCode>(
                                    Exception(getString(Res.string.openid_login_cancelled)),
                                ),
                            )
                        }
                    }
                }
        }
    }

    override fun unbind() {
        openIdLauncher?.unregister()
        openIdLauncher = null
        lifecycleScope = null
        onResultCallback = null
    }

    override fun handleIntent(
        intent: Any,
        onResult: suspend (Result<Any>) -> Unit,
    ) {
        if (intent !is IntentWithRequestCode) {
            lifecycleScope?.launch {
                onResult(
                    Result.failure<IntentWithRequestCode>(
                        IllegalArgumentException(getString(Res.string.openid_error_expected_intent)),
                    ),
                )
            } ?: throw IllegalStateException()
            return
        }

        val launcher = openIdLauncher
        if (launcher == null) {
            lifecycleScope?.launch {
                onResult(
                    Result.failure(
                        IllegalStateException(getString(Res.string.openid_error_controller_not_bounded)),
                    ),
                )
            } ?: throw IllegalStateException()
            return
        }

        onResultCallback = onResult
        openIDRequestCode = intent.requestCode
        launcher.launch(intent.intent)
    }
}
