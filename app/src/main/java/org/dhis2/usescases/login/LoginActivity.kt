package org.dhis2.usescases.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.dhis2.App
import org.dhis2.bindings.buildInfo
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.bindings.onRightDrawableClicked
import org.dhis2.commons.Constants.ACCOUNT_RECOVERY
import org.dhis2.commons.Constants.EXTRA_DATA
import org.dhis2.commons.Constants.SESSION_DIALOG_RQ
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.fingerprint.FingerPrintResult
import org.dhis2.data.fingerprint.Type
import org.dhis2.data.server.OpenIdSession
import org.dhis2.data.server.UserManager
import org.dhis2.databinding.ActivityLoginBinding
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.usescases.about.PolicyView
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.accounts.AccountsActivity
import org.dhis2.usescases.login.auth.AuthServiceModel
import org.dhis2.usescases.login.auth.OpenIdProviders
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.usescases.sync.SyncActivity
import org.dhis2.utils.NetworkUtils
import org.dhis2.utils.TestingCredential
import org.dhis2.utils.WebViewActivity
import org.dhis2.utils.WebViewActivity.Companion.WEB_VIEW_URL
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.FORGOT_CODE
import org.dhis2.utils.session.PIN_DIALOG_TAG
import org.dhis2.utils.session.PinDialog
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter
import javax.inject.Inject

const val EXTRA_SKIP_SYNC = "SKIP_SYNC"
const val EXTRA_SESSION_EXPIRED = "EXTRA_SESSION_EXPIRED"
const val EXTRA_ACCOUNT_DISABLED = "EXTRA_ACCOUNT_DISABLED"
const val IS_DELETION = "IS_DELETION"
const val ACCOUNTS_COUNT = "ACCOUNTS_COUNT"
const val RESULT_ACCOUNT_SERVER = "RESULT_ACCOUNT_SERVER"
const val RESULT_ACCOUNT_USERNAME = "RESULT_ACCOUNT_USERNAME"
const val RESULT_ACCOUNT_CLICKED = "RESULT_ACCOUNT_CLICKED"

class LoginActivity : ActivityGlobalAbstract(), LoginContracts.View {

    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var presenter: LoginViewModel

    @Inject
    lateinit var openIdProviders: OpenIdProviders

    @Inject
    lateinit var resourceManager: ResourceManager

    private var isPinScreenVisible = false
    private var qrUrl: String? = null

    private var testingCredentials: List<TestingCredential> = ArrayList()
    private var skipSync = false
    private var openIDRequestCode = -1

    companion object {
        fun bundle(
            skipSync: Boolean = false,
            accountsCount: Int = -1,
            isDeletion: Boolean = false,
            logOutReason: OpenIdSession.LogOutReason? = null,
        ): Bundle {
            return Bundle().apply {
                putBoolean(EXTRA_SKIP_SYNC, skipSync)
                putBoolean(IS_DELETION, isDeletion)
                putInt(ACCOUNTS_COUNT, accountsCount)
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

        fun accountIntentResult(
            serverUrl: String?,
            userName: String?,
            wasAccountClicked: Boolean,
        ): Intent = Intent().apply {
            serverUrl?.let { putExtra(RESULT_ACCOUNT_SERVER, serverUrl) }
            userName?.let { putExtra(RESULT_ACCOUNT_USERNAME, userName) }
            putExtra(RESULT_ACCOUNT_CLICKED, wasAccountClicked)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.LoginTheme)
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

        if ((isDeletion && accountsCount >= 1)) {
            openAccountsActivity()
        }

        skipSync = intent.getBooleanExtra(EXTRA_SKIP_SYNC, false)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.presenter = presenter
        setLoginVisibility(false)

        presenter.isDataComplete.observe(this) { this.setLoginVisibility(it) }

        presenter.isTestingEnvironment.observe(
            this,
        ) { testingEnvironment ->
            binding.root.closeKeyboard()
            binding.serverUrlEdit.setText(testingEnvironment.val0())
            binding.userNameEdit.setText(testingEnvironment.val1())
            binding.userPassEdit.setText(testingEnvironment.val2())
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
            showLoginProgress(show)
        }

        setTestingCredentials()
        setAutocompleteAdapters()
        setUpLoginInfo()
        checkMessage()
        presenter.apply {
            checkServerInfoAndShowBiometricButton()
        }

        if (!isDeletion && accountsCount == 1) {
            blockLoginInfo()
        }
    }

    private fun checkUrl(urlString: String): Boolean {
        return URLUtil.isValidUrl(urlString) &&
            Patterns.WEB_URL.matcher(urlString).matches() && urlString.toHttpUrlOrNull() != null
    }

    override fun setTestingCredentials() {
        val testingCredentialsIdentifier =
            resources.getIdentifier("testing_credentials", "raw", packageName)
        if (testingCredentialsIdentifier != -1) {
            val writer = StringWriter()
            val buffer = CharArray(1024)
            try {
                resources.openRawResource(testingCredentialsIdentifier).use { resource ->
                    val reader = BufferedReader(InputStreamReader(resource, "UTF-8"))
                    var n: Int = reader.read(buffer)
                    while (n != -1) {
                        writer.write(buffer, 0, n)
                        n = reader.read(buffer)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

            testingCredentials = Gson().fromJson(
                writer.toString(),
                object : TypeToken<List<TestingCredential>>() {}.type,
            )
            presenter.setTestingCredentials(testingCredentials)
        }
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

    override fun setUrl(url: String) {
        binding.serverUrlEdit.setText(if (!isEmpty(qrUrl)) qrUrl else url)
    }

    override fun setUser(user: String) {
        binding.userNameEdit.setText(user)
        binding.userNameEdit.setSelectAllOnFocus(true)
    }

    override fun showUnlockButton() {
        binding.unlock.visibility = View.VISIBLE
        binding.logout.visibility = View.GONE
        onUnlockClick(binding.unlock)
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

    private fun showLoginProgress(showLogin: Boolean) {
        if (showLogin) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            )
            binding.credentialLayout.visibility = View.GONE
            binding.progressLayout.visibility = View.VISIBLE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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
                mainButton = DialogButtonStyle.MainButton(textResource = R.string.yes),
                secondaryButton = DialogButtonStyle.SecondaryButton(textResource = R.string.no),
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
        ).show(supportFragmentManager, BottomSheetDialog::class.simpleName)
    }

    override fun onUnlockClick(android: View) {
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

        val (urls, users) = presenter.getAutocompleteData(testingCredentials)

        urls.let {
            val urlAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, it)
            binding.serverUrlEdit.setAdapter(urlAdapter)
        }

        users.let {
            val userAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, it)
            binding.userNameEdit.setAdapter(userAdapter)
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
            !presenter.areSameCredentials() -> {
                handleFingerPrint()
                goToNextScreen()
            }
            else -> goToNextScreen()
        }
    }

    private fun handleFingerPrint() {
        // This is commented until fingerprint login for multiuser is supported
        /* if (presenter.canHandleBiometrics() == true) {
            showInfoDialog(
                getString(R.string.biometrics_security_title),
                getString(R.string.biometrics_security_text),
                object : OnDialogClickListener {
                    override fun onPositiveClick() {
                        presenter.saveUserCredentials(
                            binding.serverUrlEdit.text.toString(),
                            binding.userNameEdit.text.toString(),
                            binding.userPassEdit.text.toString()
                        )
                        goToNextScreen()
                    }

                    override fun onNegativeClick() {
                        goToNextScreen()
                    }
                }
            )
           goToNextScreen()
        } else {
            presenter.saveUserCredentials(
                binding.serverUrlEdit.text.toString(),
                binding.userNameEdit.text.toString(),
                ""
            )
            goToNextScreen()
        } */
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
        // This is commented until fingerprint login for multiuser is supported
        // binding.biometricButton.visibility = View.VISIBLE
    }

    private val requestQRScanner = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (it.resultCode == RESULT_OK) {
            qrUrl = it.data?.getStringExtra(EXTRA_DATA)
            qrUrl?.let { setUrl(it) }
        }
    }

    private val requestAccount = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val wasAccountClicked = result.data?.extras?.getBoolean(RESULT_ACCOUNT_CLICKED) ?: false
            setAccount(
                result.data?.extras?.getString(RESULT_ACCOUNT_SERVER) ?: getDefaultServerProtocol(),
                result.data?.extras?.getString(RESULT_ACCOUNT_USERNAME),
                wasAccountClicked,
            )
        }
        if (result.resultCode == RESULT_CANCELED) {
            resetLoginInfo()
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

    /*
     * TODO: [Pending confirmation] Remove comment to set skipSync. This way the user will go to the home screen.
     * */
    private fun setAccount(serverUrl: String?, userName: String?, wasAccountClicked: Boolean) {
        serverUrl?.let { binding.serverUrlEdit.setText(it) }
        binding.userNameEdit.setText(userName ?: "")
        presenter.setAccountInfo(serverUrl, userName)
        binding.userPassEdit.text = null
//        skipSync = wasAccountClicked
        if (wasAccountClicked) {
            blockLoginInfo()
        } else {
            resetLoginInfo()
        }
    }

    override fun showCredentialsData(result: FingerPrintResult, vararg args: String) {
        if (result.type == Type.SUCCESS) {
            binding.serverUrlEdit.setText(args[0])
            binding.userNameEdit.setText(args[1])
            binding.userPassEdit.setText(args[2])
        } else if (result.type == Type.ERROR && args[0] != getString(R.string.cancel)) {
            showInfoDialog(getString(R.string.biometrics_dialog_title), args[0])
        }
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

    private fun setUpLoginInfo() {
        binding.appBuildInfo.text = buildInfo()
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

    override fun openAccountsActivity() {
        requestAccount.launch(Intent(this, AccountsActivity::class.java))
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
