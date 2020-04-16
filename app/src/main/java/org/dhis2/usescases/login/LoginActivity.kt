package org.dhis2.usescases.login

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
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
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.infinum.goldfinger.Goldfinger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.HttpUrl
import org.dhis2.App
import org.dhis2.Bindings.app
import org.dhis2.Bindings.buildInfo
import org.dhis2.Bindings.onRightDrawableClicked
import org.dhis2.R
import org.dhis2.data.server.UserManager
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.ActivityLoginBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.usescases.sync.SyncActivity
import org.dhis2.utils.*
import org.dhis2.utils.Constants.ACCOUNT_RECOVERY
import org.dhis2.utils.Constants.RQ_QR_SCANNER
import org.dhis2.utils.WebViewActivity.Companion.WEB_VIEW_URL
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.FORGOT_CODE
import org.dhis2.utils.session.PIN_DIALOG_TAG
import org.dhis2.utils.session.PinDialog
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter
import javax.inject.Inject

class LoginActivity : ActivityGlobalAbstract(), LoginContracts.View {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var fingerPrintDialog: Dialog

    @Inject
    lateinit var presenter: LoginPresenter

    private var users: MutableList<String>? = null
    private var urls: MutableList<String>? = null

    private var isPinScreenVisible = false
    private var qrUrl: String? = null

    private var testingCredentials: List<TestingCredential> = ArrayList()
    var userManager: UserManager? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        var loginComponent = app().loginComponent()
        if (loginComponent == null) {
            // in case if we don't have cached presenter
            loginComponent = (applicationContext as App).createLoginComponent(this)
        }
        val serverComponent = (applicationContext as App).serverComponent
        serverComponent?.let {
            userManager = serverComponent.userManager()
        }

        loginComponent.inject(this)

        super.onCreate(savedInstanceState)
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.presenter = presenter
        binding.loginModel = loginViewModel
        setLoginVisibility(false)

        loginViewModel.isDataComplete.observe(
            this,
            Observer<Boolean> { this.setLoginVisibility(it) }
        )

        loginViewModel.isTestingEnvironment.observe(
            this,
            Observer<Trio<String, String, String>> { testingEnvironment ->
                binding.serverUrlEdit.setText(testingEnvironment.val0())
                binding.userNameEdit.setText(testingEnvironment.val1())
                binding.userPassEdit.setText(testingEnvironment.val2())
            }
        )

        binding.serverUrlEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkUrl(binding.serverUrlEdit.text.toString())) {
                    binding.accountRecovery.visibility = View.VISIBLE
                } else {
                    binding.accountRecovery.visibility = View.GONE
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

        setTestingCredentials()
        setAutocompleteAdapters()
        setUpFingerPrintDialog()
        setUpLoginInfo()
    }

    override fun setUpFingerPrintDialog() {
        fingerPrintDialog = MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
            .setTitle(R.string.fingerprint_title)
            .setMessage(R.string.fingerprint_message)
            .setCancelable(false)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                presenter.stopReadingFingerprint()
                dialog.dismiss()
            }
            .create()
    }

    private fun checkUrl(urlString: String): Boolean {
        return URLUtil.isValidUrl(urlString) &&
            Patterns.WEB_URL.matcher(urlString).matches() &&
            HttpUrl.parse(urlString) != null
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
                object : TypeToken<List<TestingCredential>>() {}.type
            )
            loginViewModel.setTestingCredentials(testingCredentials)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.apply {
            init(userManager)
            checkServerInfoAndShowBiometricButton()
        }
        NetworkUtils.isGooglePlayServicesAvailable(this)
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
        if (NetworkUtils.isOnline(this)) {
            startActivity(SyncActivity::class.java, null, true, true, null)
        } else {
            startActivity(MainActivity::class.java, null, true, true, null)
        }
    }

    override fun setUrl(url: String) {
        binding.serverUrlEdit.setText(if (!isEmpty(qrUrl)) qrUrl else url)
    }

    override fun setUser(user: String) {
        binding.userNameEdit.setText(user)
        binding.userNameEdit.setSelectAllOnFocus(true)
    }

    override fun showUnlockButton() {
        binding.unlockLayout.visibility = View.VISIBLE
        onUnlockClick(binding.unlockLayout)
    }

    override fun renderError(throwable: Throwable) {
        showInfoDialog(
            getString(R.string.login_error),
            D2ErrorUtils.getErrorMessage(this, throwable)
        )
    }

    override fun handleLogout() {
        recreate()
    }

    override fun setLoginVisibility(isVisible: Boolean) {
        binding.login.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun showLoginProgress(showLogin: Boolean) {
        if (showLogin) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            binding.credentialLayout.visibility = View.GONE
            binding.progressLayout.visibility = View.VISIBLE

            presenter.logIn(
                binding.serverUrl.editText?.text.toString(),
                binding.userName.editText?.text.toString(),
                binding.userPass.editText?.text.toString()
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            binding.credentialLayout.visibility = View.VISIBLE
            binding.progressLayout.visibility = View.GONE
        }
    }

    override fun alreadyAuthenticated() {
        startActivity(MainActivity::class.java, null, true, true, null)
    }

    override fun showCrashlyticsDialog() {
        showInfoDialog(
            getString(R.string.send_user_name_title), getString(R.string.send_user_name_mesage),
            getString(R.string.action_agree), getString(R.string.cancel),
            object : OnDialogClickListener {
                override fun onPossitiveClick(alertDialog: AlertDialog) {
                    sharedPreferences.edit().putBoolean(Constants.USER_ASKED_CRASHLYTICS, true)
                        .apply()
                    sharedPreferences.edit()
                        .putString(Constants.USER, binding.userName.editText?.text.toString())
                        .apply()
                    showLoginProgress(true)
                }

                override fun onNegativeClick(alertDialog: AlertDialog) {
                    sharedPreferences.edit().putBoolean(Constants.USER_ASKED_CRASHLYTICS, true)
                        .apply()
                    showLoginProgress(true)
                }
            }
        )?.show()
    }

    override fun onUnlockClick(android: View) {
        PinDialog(
            PinDialog.Mode.ASK,
            true,
            {
                startActivity(MainActivity::class.java, null, true, true, null)
            },
            {
                analyticsHelper.setEvent(FORGOT_CODE, CLICK, FORGOT_CODE)
            }
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

    override fun saveUsersData() {
        (context.applicationContext as App).createUserComponent()

        if (presenter.canHandleBiometrics() == true &&
            !presenter.areSameCredentials(
                binding.serverUrlEdit.text.toString(),
                binding.userNameEdit.text.toString(),
                binding.userPassEdit.text.toString()
            )
        ) {
            showInfoDialog(
                getString(R.string.biometrics_security_title),
                getString(R.string.biometrics_security_text),
                object : OnDialogClickListener {
                    override fun onPossitiveClick(alertDialog: AlertDialog) {
                        presenter.saveUserCredentials(
                            binding.serverUrlEdit.text.toString(),
                            binding.userNameEdit.text.toString(),
                            binding.userPassEdit.text.toString()
                        )
                        goToNextScreen()
                    }

                    override fun onNegativeClick(alertDialog: AlertDialog) {
                        goToNextScreen()
                    }
                }
            )?.show()
        } else {
            goToNextScreen()
        }
    }

    override fun onBackPressed() {
        if (isPinScreenVisible) {
            binding.pinLayout.root.visibility = View.GONE
            isPinScreenVisible = false
        } else {
            super.onBackPressed()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RQ_QR_SCANNER && resultCode == Activity.RESULT_OK) {
            qrUrl = data?.getStringExtra(Constants.EXTRA_DATA)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showBiometricButton() {
        binding.biometricButton.visibility = View.VISIBLE
    }

    override fun showCredentialsData(type: Goldfinger.Type, vararg args: String) {
        hideFingerprintDialog()
        if (type == Goldfinger.Type.SUCCESS) {
            binding.serverUrlEdit.setText(args[0])
            binding.userNameEdit.setText(args[1])
            binding.userPassEdit.setText(args[2])
            showLoginProgress(true)
        } else if (type == Goldfinger.Type.ERROR) {
            showInfoDialog(getString(R.string.biometrics_dialog_title), args[0])
        }
    }

    override fun showEmptyCredentialsMessage() {
        showInfoDialog(
            getString(R.string.biometrics_dialog_title),
            getString(R.string.biometrics_first_use_text)
        )
    }

    override fun openAccountRecovery() {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WEB_VIEW_URL, binding.serverUrlEdit.text.toString() + ACCOUNT_RECOVERY)
        startActivity(intent)
    }

    override fun displayAlertDialog() {
        MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
            .setTitle(R.string.login_server_info_title)
            .setMessage(R.string.login_server_info_message)
            .setPositiveButton(R.string.action_accept, null)
            .show()
    }

    override fun navigateToQRActivity() {
        Intent(context, ScanActivity::class.java).apply {
            startActivityForResult(this, RQ_QR_SCANNER)
        }
    }

    override fun showFingerprintDialog() {
        fingerPrintDialog.show()
    }

    override fun hideFingerprintDialog() {
        fingerPrintDialog.hide()
    }

    private fun setUpLoginInfo() {
        binding.appBuildInfo.text = buildInfo()
    }

    override fun getDefaultServerProtocol(): String =
        getString(R.string.login_https)
}
