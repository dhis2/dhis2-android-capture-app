package org.dhis2.usescases.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.dhis2.App
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.bindings.buildInfo
import org.dhis2.bindings.onRightDrawableClicked
import org.dhis2.commons.Constants.ACCOUNT_RECOVERY
import org.dhis2.commons.Constants.EXTRA_DATA
import org.dhis2.commons.Constants.SESSION_DIALOG_RQ
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.server.OpenIdSession
import org.dhis2.data.server.UserManager
import org.dhis2.databinding.ActivityLoginBinding
import org.dhis2.mobile.login.main.ui.screen.LoginScreen
import org.dhis2.usescases.about.PolicyView
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.auth.AuthServiceModel
import org.dhis2.usescases.login.auth.OpenIdProviders
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.usescases.sync.SyncActivity
import org.dhis2.utils.NetworkUtils
import org.dhis2.utils.WebViewActivity
import org.dhis2.utils.WebViewActivity.Companion.WEB_VIEW_URL
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.FORGOT_CODE
import org.dhis2.utils.session.PIN_DIALOG_TAG
import org.dhis2.utils.session.PinDialog
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

const val EXTRA_SKIP_SYNC = "SKIP_SYNC"
const val EXTRA_SESSION_EXPIRED = "EXTRA_SESSION_EXPIRED"
const val EXTRA_ACCOUNT_DISABLED = "EXTRA_ACCOUNT_DISABLED"
const val IS_DELETION = "IS_DELETION"
const val ACCOUNTS_COUNT = "ACCOUNTS_COUNT"
const val FROM_SPLASH = "FROM_SPLASH"

class LoginActivity : ActivityGlobalAbstract(), LoginContracts.View {

    override var handleEdgeToEdge = false

    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var presenter: LoginViewModel

    @Inject
    lateinit var openIdProviders: OpenIdProviders

    @Inject
    lateinit var resourceManager: ResourceManager

    private var isPinScreenVisible = false
    private var qrUrl: String? = null

    private var skipSync = false
    private var openIDRequestCode = -1

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                val fileType = with(contentResolver) {
                    MimeTypeMap.getSingleton().getExtensionFromMimeType(getType(uri))
                }
                val file = File.createTempFile("importedDb", fileType)
                val inputStream = contentResolver.openInputStream(uri)!!
                try {
                    FileOutputStream(file, false).use { outputStream ->
                        var read: Int
                        val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (inputStream.read(bytes).also { read = it } != -1) {
                            outputStream.write(bytes, 0, read)
                        }
                    }
                } catch (e: IOException) {
                    Timber.e("Failed to load file: %s", e.message.toString())
                }
                if (file.exists()) {
                    presenter.onImportDataBase(file)
                }
            }
        }

    override fun onDbImportFinished(isSuccess: Boolean) {
        showLoginProgress(false)
        if (isSuccess) {
            blockLoginInfo()
        }
    }

    companion object {
        fun bundle(
            skipSync: Boolean = false,
            accountsCount: Int = -1,
            isDeletion: Boolean = false,
            logOutReason: OpenIdSession.LogOutReason? = null,
            fromSplash: Boolean = false,
        ): Bundle {
            return Bundle().apply {
                putBoolean(EXTRA_SKIP_SYNC, skipSync)
                putBoolean(IS_DELETION, isDeletion)
                putInt(ACCOUNTS_COUNT, accountsCount)
                putBoolean(FROM_SPLASH, fromSplash)
                when (logOutReason) {
                    OpenIdSession.LogOutReason.OPEN_ID -> putBoolean(EXTRA_SESSION_EXPIRED, true)
                    OpenIdSession.LogOutReason.DISABLED_ACCOUNT -> putBoolean(
                        EXTRA_ACCOUNT_DISABLED,
                        true,
                    )

                    null -> {
                        // Nothing to do in this case
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.WHITE),
        )
        val loginComponent = app().loginComponent() ?: app().createLoginComponent(
            LoginModule(
                view = this,
                viewModelStoreOwner = this,
                userManager = app().serverComponent?.userManager(),
            ),
        )

        loginComponent.inject(this)

        super.onCreate(savedInstanceState)
        val accountsCount = intent.getIntExtra(ACCOUNTS_COUNT, -1)
        val isDeletion = intent.getBooleanExtra(IS_DELETION, false)

        skipSync = intent.getBooleanExtra(EXTRA_SKIP_SYNC, false)

        setContent {
            DHIS2Theme {
                LoginScreen(
                    versionName = buildInfo(),
                    onImportDatabase = {
                        showLoginProgress(false, getString(R.string.importing_database))
                        val intent = Intent()
                        intent.type = "*/*"
                        intent.action = Intent.ACTION_GET_CONTENT
                        filePickerLauncher.launch(intent)
                    },
                    legacyLoginContent = { server, username ->
                        AndroidView(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = Radius.L, topEnd = Radius.L)),
                            factory = {
                                ActivityLoginBinding.inflate(LayoutInflater.from(this)).also {
                                    binding = it
                                    initLegacy(isDeletion, accountsCount)
                                    setAccount(server, username)
                                }.root
                            },
                            update = {
                                // no-op
                            },
                        )
                    },
                )
            }
        }
    }

    private fun initLegacy(isDeletion: Boolean, accountsCount: Int) {
        val fromSplash = intent.getBooleanExtra(FROM_SPLASH, false)

        provideBiometricButton()

        binding.presenter = presenter
        setLoginVisibility(false)

        presenter.isDataComplete.observe(this) { this.setLoginVisibility(it) }

        presenter.isTestingEnvironment.observe(
            this,
        ) { testingEnvironment ->
            binding.root.closeKeyboard()
            binding.serverUrlEdit.setText(testingEnvironment.first)
            binding.userNameEdit.setText(testingEnvironment.second)
            binding.userPassEdit.setText(testingEnvironment.third)
        }

        openIdProviders.loadOpenIdProvider {
            showLoginOptions(it.takeIf { it.hasConfiguration() })
        }

        binding.serverUrlEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkUrl(binding.serverUrlEdit.text.toString())) {
                    binding.accountRecovery.visibility = View.VISIBLE
                    binding.loginOpenId.isEnabled = true
                } else {
                    binding.accountRecovery.visibility = View.GONE
                    binding.loginOpenId.isEnabled = false
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // nothing
            }
        })

        binding.serverUrlEdit.onRightDrawableClicked { presenter.onQRClick() }

        binding.clearPassButton.setOnClickListener { binding.userPassEdit.text = null }
        binding.clearUserNameButton.setOnClickListener { binding.userNameEdit.text = null }
        binding.clearUrl.setOnClickListener { binding.serverUrlEdit.text = null }

        presenter.loginProgressVisible.observe(this) { show ->
            showLoginProgress(show, getString(R.string.authenticating))
        }

        setAutocompleteAdapters()
        checkMessage()
        presenter.apply {
            checkServerInfoAndShowBiometricButton()
            canLoginWithBiometrics.observe(this@LoginActivity) {
                if (it && fromSplash) {
                    presenter.authenticateWithBiometric()
                }
            }
        }

        if (!isDeletion && accountsCount == 1) {
            blockLoginInfo()
        }
    }

    private fun provideBiometricButton() {
        binding.biometricButton.setContent {
            val displayBiometric by presenter.canLoginWithBiometrics.observeAsState(false)
            if (displayBiometric) {
                DHIS2Theme {
                    IconButton(
                        onClick = {
                            presenter.authenticateWithBiometric()
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fingerprint),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = stringResource(id = R.string.fingerprint_title),
                        )
                    }
                }
            }
        }
    }

    private fun checkUrl(urlString: String): Boolean {
        return URLUtil.isValidUrl(urlString) &&
            Patterns.WEB_URL.matcher(urlString).matches() && urlString.toHttpUrlOrNull() != null
    }

    override fun onPause() {
        presenter.onDestroy()
        super.onPause()
    }

    override fun onDestroy() {
        (applicationContext as App).releaseLoginComponent()
        super.onDestroy()
    }

    override fun goToNextScreen() {
        if (isNetworkAvailable() && !skipSync) {
            startActivity(SyncActivity::class.java, null, true, true, null)
        } else {
            startActivity(MainActivity::class.java, null, true, true, null)
        }
    }

    override fun isNetworkAvailable(): Boolean {
        return NetworkUtils.isOnline(this)
    }

    override fun setUrl(url: String?) {
        binding.serverUrlEdit.setText(if (!isEmpty(qrUrl)) qrUrl else url)
    }

    override fun setUser(user: String?) {
        binding.userNameEdit.setText(user)
        binding.userNameEdit.setSelectAllOnFocus(true)
    }

    override fun showUnlockButton() {
        if (::binding.isInitialized) {
            binding.unlock.visibility = View.VISIBLE
            binding.logout.visibility = View.GONE
        }
        onUnlockClick()
    }

    override fun renderError(throwable: Throwable) {
        showInfoDialog(
            getString(R.string.login_error),
            resourceManager.parseD2Error(throwable),
        )
    }

    override fun handleLogout() {
        recreate()
    }

    override fun setLoginVisibility(isVisible: Boolean) {
        binding.login.isEnabled = isVisible
    }

    private fun showLoginProgress(showLogin: Boolean, message: String? = null) {
        if (showLogin) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            )
            binding.credentialLayout.visibility = View.GONE
            binding.progressLayout.visibility = View.VISIBLE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            binding.progressMessage.text = message
            binding.credentialLayout.visibility = View.VISIBLE
            binding.progressLayout.visibility = View.GONE
        }
    }

    override fun alreadyAuthenticated() {
        startActivity(MainActivity::class.java, null, true, true, null)
    }

    private fun showCrashlyticsDialog() {
        BottomSheetDialog(
            BottomSheetDialogUiModel(
                title = getString(R.string.improve_app_msg_title),
                message = getString(R.string.improve_app_msg_text),
                clickableWord = getString(R.string.improve_app_msg_clickable_word),
                iconResource = R.drawable.ic_line_chart,
                headerTextAlignment = TextAlign.Start,
                mainButton = DialogButtonStyle.MainButton(textResource = R.string.yes),
                secondaryButton = DialogButtonStyle.SecondaryButton(
                    textResource = R.string.not_now,
                    buttonStyle = ButtonStyle.OUTLINED,
                ),
            ),
            onMainButtonClicked = {
                presenter.grantTrackingPermissions(true)
                context.app().initCrashController()
                onLoginDataUpdated(false)
            },
            onSecondaryButtonClicked = {
                presenter.grantTrackingPermissions(false)
                onLoginDataUpdated(false)
            },
            onMessageClick = {
                navigateToPrivacyPolicy()
            },
            showTopDivider = false,
            showBottomDivider = true,
        ).show(supportFragmentManager, BottomSheetDialog::class.simpleName)
    }

    override fun onUnlockClick() {
        PinDialog(
            PinDialog.Mode.ASK,
            false,
            {
                startActivity(MainActivity::class.java, null, true, true, null)
            },
            {
                analyticsHelper.setEvent(FORGOT_CODE, CLICK, FORGOT_CODE)
                binding.unlock.visibility = View.GONE
                binding.logout.visibility = View.GONE
            },
        )
            .show(supportFragmentManager, PIN_DIALOG_TAG)
    }

    override fun onLogoutClick(android: View) {
        presenter.logOut()
    }

    override fun setAutocompleteAdapters() {
        binding.serverUrlEdit.dropDownWidth = resources.displayMetrics.widthPixels
        binding.userNameEdit.dropDownWidth = resources.displayMetrics.widthPixels

        presenter.autoCompleteData.observe(this) { (urls, users) ->
            urls.let {
                val urlAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, it)
                binding.serverUrlEdit.setAdapter(urlAdapter)
            }

            users.let {
                val userAdapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, it)
                binding.userNameEdit.setAdapter(userAdapter)
            }
        }
    }

    override fun saveUsersData(displayTrackingMessage: Boolean, isInitialSyncDone: Boolean) {
        app().createUserComponent()
        skipSync = isInitialSyncDone
        onLoginDataUpdated(displayTrackingMessage)
    }

    private fun onLoginDataUpdated(displayTrackingMessage: Boolean) {
        when {
            displayTrackingMessage -> showCrashlyticsDialog()
            presenter.shouldAskForBiometrics() -> showBiometricDialog()
            else -> {
                presenter.saveUserCredentials {
                    goToNextScreen()
                }
            }
        }
    }

    private fun showBiometricDialog() {
        BottomSheetDialog(
            BottomSheetDialogUiModel(
                title = getString(R.string.biometrics_login_title),
                message = getString(R.string.biometrics_login_text),
                iconResource = R.drawable.ic_fingerprint,
                mainButton = DialogButtonStyle.MainButton(textResource = R.string.yes),
                secondaryButton = DialogButtonStyle.SecondaryButton(
                    textResource = R.string.not_now,
                    buttonStyle = ButtonStyle.OUTLINED,
                ),
            ),
            showTopDivider = true,
            onMainButtonClicked = {
                presenter.saveUserCredentials(binding.userPassEdit.text.toString()) {
                    onLoginDataUpdated(false)
                }
            },
            onSecondaryButtonClicked = {
                presenter.saveUserCredentials {
                    onLoginDataUpdated(false)
                }
            },
        ).show(supportFragmentManager, BottomSheetDialog::class.simpleName)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isPinScreenVisible) {
            binding.pinLayout.root.visibility = View.GONE
            isPinScreenVisible = false
        } else {
            super.onBackPressed()
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == openIDRequestCode && resultCode == Activity.RESULT_OK) {
            data?.let {
                presenter.handleAuthResponseData(
                    binding.serverUrlEdit.text.toString(),
                    data,
                    requestCode,
                )
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showBiometricButton() {
        binding.biometricButton.visibility = View.VISIBLE
    }

    private val requestQRScanner = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (it.resultCode == RESULT_OK) {
            qrUrl = it.data?.getStringExtra(EXTRA_DATA)
            qrUrl?.let { setUrl(it) }
        }
    }

    override fun initLogin(): UserManager {
        return app().createServerComponent().userManager()
    }

    private fun resetLoginInfo() {
        binding.serverUrlEdit.alpha = 1f
        binding.userNameEdit.alpha = 1f
        binding.serverUrlEdit.isEnabled = true
        binding.userNameEdit.isEnabled = true
        binding.clearUrl.visibility = View.VISIBLE
        binding.clearUserNameButton.visibility = View.VISIBLE
    }

    private fun blockLoginInfo() {
        binding.serverUrlEdit.alpha = 0.5f
        binding.userNameEdit.alpha = 0.5f
        binding.serverUrlEdit.isEnabled = false
        binding.userNameEdit.isEnabled = false
        binding.clearUrl.visibility = View.GONE
        binding.clearUserNameButton.visibility = View.GONE
    }

    private fun setAccount(serverUrl: String?, userName: String?) {
        binding.serverUrlEdit.setText(serverUrl ?: "")
        binding.userNameEdit.setText(userName ?: "")
        presenter.setAccountInfo(serverUrl, userName)
        binding.userPassEdit.text = null
        if (serverUrl?.isNotEmpty() == true && userName?.isNotEmpty() == true) {
            blockLoginInfo()
        } else {
            resetLoginInfo()
        }
    }

    override fun showCredentialsData(vararg args: String) {
        binding.serverUrlEdit.setText(args[0])
        binding.userNameEdit.setText(args[1])
        binding.userPassEdit.setText(args[2])
    }

    override fun showEmptyCredentialsMessage() {
        showInfoDialog(
            getString(R.string.biometrics_dialog_title),
            getString(R.string.biometrics_first_use_text),
        )
    }

    override fun openAccountRecovery() {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WEB_VIEW_URL, binding.serverUrlEdit.text.toString() + ACCOUNT_RECOVERY)
        startActivity(intent)
    }

    override fun navigateToQRActivity() {
        requestQRScanner.launch(Intent(context, ScanActivity::class.java))
    }

    override fun getDefaultServerProtocol(): String = getString(R.string.login_https)

    private fun showLoginOptions(authServiceModel: AuthServiceModel?) {
        authServiceModel?.let {
            binding.serverUrlEdit.setText(authServiceModel.serverUrl)
            binding.loginOpenId.visibility = View.VISIBLE
            binding.loginOpenId.text = authServiceModel.loginLabel
            binding.loginOpenId.setOnClickListener {
                presenter.openIdLogin(authServiceModel.toOpenIdConfig())
            }
        }
    }

    override fun openOpenIDActivity(intentData: IntentWithRequestCode?) {
        intentData?.let {
            openIDRequestCode = intentData.requestCode
            startActivityForResult(intentData.intent, intentData.requestCode)
        }
    }

    private fun navigateToPrivacyPolicy() {
        activity?.let {
            startActivity(Intent(it, PolicyView::class.java))
        }
    }

    private fun checkMessage() {
        if (intent.getBooleanExtra(EXTRA_SESSION_EXPIRED, false)) {
            showSessionExpired()
        } else if (intent.getBooleanExtra(EXTRA_ACCOUNT_DISABLED, false)) {
            showAccountDisabled()
        }
    }

    private fun showSessionExpired() {
        val sessionDialog = CustomDialog(
            this,
            getString(R.string.openid_session_expired),
            getString(R.string.openid_session_expired_message),
            getString(R.string.action_accept),
            null,
            SESSION_DIALOG_RQ,
            null,
        )
        sessionDialog.setCancelable(false)
        sessionDialog.show()
    }

    private fun showAccountDisabled() {
        val sessionDialog = CustomDialog(
            this,
            getString(R.string.account_disable_title),
            getString(R.string.account_disable_message),
            getString(R.string.action_accept),
            null,
            SESSION_DIALOG_RQ,
            null,
        )
        sessionDialog.setCancelable(false)
        sessionDialog.show()
    }

    override fun showNoConnectionDialog() {
        val dialog = CustomDialog(
            this,
            getString(R.string.network_unavailable),
            getString(R.string.no_network_to_recover_account),
            getString(R.string.action_ok),
            null,
            CustomDialog.NO_RQ_CODE,
            null,
        )
        dialog.show()
    }
}
