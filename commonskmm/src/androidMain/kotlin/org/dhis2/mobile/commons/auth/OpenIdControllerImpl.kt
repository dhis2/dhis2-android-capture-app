package org.dhis2.mobile.commons.auth

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode

class OpenIdControllerImpl(
    context: FragmentActivity,
) : OpenIdController {
    private var openIDRequestCode = -1
    private var onResultCallback: (Any) -> Unit = {}

    private val openIdLauncher =
        context.activityResultRegistry.register(
            key = "OPEN_ID_LAUNCHER",
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            result.data?.let {
                val intentResult =
                    IntentWithRequestCode(
                        intent = it,
                        requestCode = openIDRequestCode,
                    )
                onResultCallback(intentResult)
            }
        }

    override fun handleIntent(
        intent: Any,
        onResult: suspend (Any) -> Unit,
    ) {
        if (intent !is IntentWithRequestCode) return
        openIDRequestCode = intent.requestCode
        openIdLauncher.launch(intent.intent)
    }
}
