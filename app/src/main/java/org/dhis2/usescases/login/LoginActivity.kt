package org.dhis2.usescases.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils.isEmpty
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andrognito.pinlockview.PinLockListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.ActivityLoginBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.sync.SyncActivity
import org.dhis2.utils.*
import org.dhis2.utils.Constants.RQ_QR_SCANNER
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter
import java.util.*
import javax.inject.Inject


class LoginActivity : ActivityGlobalAbstract(), LoginContracts.View {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var loginViewModel: LoginViewModel

    @Inject
    lateinit var presenter: LoginContracts.Presenter

    private var users: MutableList<String>? = null
    private var urls: MutableList<String>? = null

    private var isPinScreenVisible = false
    private var qrUrl: String? = null

    private var testingCredentials: List<TestingCredential> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {

        var loginComponent = (applicationContext as App).loginComponent()
        if (loginComponent == null) {
            // in case if we don't have cached presenter
            loginComponent = (applicationContext as App).createLoginComponent()
        }
        loginComponent.inject(this)

        super.onCreate(savedInstanceState)
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.presenter = presenter
        binding.loginModel = loginViewModel
        setLoginVisibility(false)


        loginViewModel.isDataComplete.observe(this, Observer<Boolean> { this.setLoginVisibility(it) })
        loginViewModel.isTestingEnvironment.observe(this, Observer<Trio<String, String, String>> { testingEnvironment ->
            binding.serverUrlEdit.setText(testingEnvironment.val0())
            binding.userNameEdit.setText(testingEnvironment.val1())
            binding.userPassEdit.setText(testingEnvironment.val2())
        })

        setTestingCredentials()
        setAutocompleteAdapters()

    }

    private fun setTestingCredentials() {
        val testingCredentialsIdentifier = resources.getIdentifier("testing_credentials", "raw", packageName)
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

            testingCredentials = Gson().fromJson(writer.toString(), object : TypeToken<List<TestingCredential>>() {}.type)
            loginViewModel.setTestingCredentials(testingCredentials)
        }
    }


    override fun onResume() {
        super.onResume()
        presenter.init(this)
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
        } else
            startActivity(MainActivity::class.java, null, true, true, null)
    }

    override fun switchPasswordVisibility() {
        if (binding.userPassEdit.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            ContextCompat.getDrawable(this, R.drawable.ic_visibility)?.let {
                binding.visibilityButton.setImageDrawable(
                        ColorUtils.tintDrawableWithColor(
                                it,
                                ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY)))
            }
            binding.userPassEdit.inputType = InputType.TYPE_CLASS_TEXT
        } else {
            ContextCompat.getDrawable(this, R.drawable.ic_visibility_off)?.let {
                binding.visibilityButton.setImageDrawable(
                        ColorUtils.tintDrawableWithColor(
                                it,
                                ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY)))
                binding.userPassEdit.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        binding.userPassEdit.text?.let {
            binding.userPassEdit.setSelection(it.length)
        }
    }

    override fun setUrl(url: String) {
        binding.serverUrlEdit.setText(if (!isEmpty(qrUrl)) qrUrl else url)
    }

    override fun showUnlockButton() {
        binding.unlockLayout?.visibility = View.VISIBLE
    }

    override fun renderError(throwable: Throwable) {
        showInfoDialog(getString(R.string.login_error), D2ErrorUtils.getErrorMessage(this, throwable))
    }

    override fun handleLogout() {
        recreate()
    }

    override fun setLoginVisibility(isVisible: Boolean) {
        binding.login.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun showLoginProgress(showLogin: Boolean) {
        if (showLogin) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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

    override fun showCrashlyticsDialog() {
        showInfoDialog(getString(R.string.send_user_name_title), getString(R.string.send_user_name_mesage),
                getString(R.string.action_agree), getString(R.string.cancel),
                object : OnDialogClickListener {
                    override fun onPossitiveClick(alertDialog: AlertDialog) {
                        sharedPreferences.edit().putBoolean(Constants.USER_ASKED_CRASHLYTICS, true).apply()
                        sharedPreferences.edit().putString(Constants.USER, binding.userName.editText?.text.toString()).apply()
                        showLoginProgress(true)
                    }

                    override fun onNegativeClick(alertDialog: AlertDialog) {
                        sharedPreferences.edit().putBoolean(Constants.USER_ASKED_CRASHLYTICS, true).apply()
                        showLoginProgress(true)
                    }
                })?.show()
    }

    override fun onUnlockClick(android: View) {
        binding.pinLayout.pinLockView.attachIndicatorDots(binding.pinLayout.indicatorDots)
        binding.pinLayout.pinLockView.setPinLockListener(object : PinLockListener {
            override fun onComplete(pin: String) {
                presenter.unlockSession(pin)
            }

            override fun onEmpty() {

            }

            override fun onPinChange(pinLength: Int, intermediatePin: String) {

            }
        })
        binding.pinLayout.root.visibility = View.VISIBLE
        isPinScreenVisible = true
    }

    override fun onLogoutClick(android: View) {
        presenter.logOut()
    }

    override fun setAutocompleteAdapters() {

        urls = getListFromPreference(Constants.PREFS_URLS)
        users = getListFromPreference(Constants.PREFS_USERS)

        urls?.let {
            if (!it.contains(Constants.URL_TEST_229))
                it.add(Constants.URL_TEST_229)
            if (!it.contains(Constants.URL_TEST_230))
                it.add(Constants.URL_TEST_230)
            for (testingCredential in testingCredentials) {
                if (!it.contains(testingCredential.server_url))
                    it.add(testingCredential.server_url)
            }
        }

        saveListToPreference(Constants.PREFS_URLS, urls)

        users?.let {
            if (!it.contains(Constants.USER_TEST_ANDROID))
                it.add(Constants.USER_TEST_ANDROID)
        }

        saveListToPreference(Constants.PREFS_USERS, users)

        urls?.let {
            val urlAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, it)
            binding.serverUrlEdit.setAdapter(urlAdapter)
        }

        users?.let {
            val userAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, it)
            binding.userNameEdit.setAdapter(userAdapter)
        }
    }

    override fun saveUsersData() {
        urls?.let {
            if (!it.contains(binding.serverUrlEdit.text.toString())) {
                it.add(binding.serverUrlEdit.text.toString())
                saveListToPreference(Constants.PREFS_URLS, it)
            }
        }

        users?.let {
            if (!it.contains(binding.userNameEdit.text.toString())) {
                it.add(binding.userNameEdit.text.toString())
                saveListToPreference(Constants.PREFS_USERS, it)
            }
        }

        if (false && presenter.canHandleBiometrics() ?: false && //TODO: Remove false when green light
                !BiometricStorage.areCredentialsSet() && !BiometricStorage.areSameCredentials(
                        binding.serverUrlEdit.text?.toString(),
                        binding.userNameEdit.text?.toString(),
                        binding.userPassEdit.text?.toString())) {
            showInfoDialog(getString(R.string.biometrics_security_title),
                    getString(R.string.biometrics_security_text),
                    object : OnDialogClickListener {
                        override fun onPossitiveClick(alertDialog: AlertDialog) {
                            BiometricStorage.saveUserCredentials(
                                    binding.serverUrlEdit.text?.toString(),
                                    binding.userNameEdit.text?.toString(),
                                    binding.userPassEdit.text?.toString())
                            goToNextScreen()
                        }

                        override fun onNegativeClick(alertDialog: AlertDialog) {
                            goToNextScreen()
                        }
                    })?.show()
        } else
            goToNextScreen()

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
    }

    //region FingerPrint
    override fun showBiometricButton() {
        binding.biometricButton.visibility = View.VISIBLE
    }

    override fun checkSecuredCredentials() {
        if (SecurePreferences.contains(Constants.SECURE_SERVER_URL) &&
                SecurePreferences.contains(Constants.SECURE_USER_NAME) &&
                SecurePreferences.contains(Constants.SECURE_PASS)) {
            binding.serverUrlEdit.setText(SecurePreferences.getStringValue(Constants.SECURE_SERVER_URL, null))
            binding.userNameEdit.setText(SecurePreferences.getStringValue(Constants.SECURE_USER_NAME, null))
            binding.userPassEdit.setText(SecurePreferences.getStringValue(Constants.SECURE_PASS, null))
            showLoginProgress(true)
        } else
            showInfoDialog(getString(R.string.biometrics_dialog_title), getString(R.string.biometrics_first_use_text))
    }
//endregion


}