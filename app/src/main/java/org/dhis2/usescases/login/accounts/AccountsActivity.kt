package org.dhis2.usescases.login.accounts

import android.content.Intent
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.livedata.observeAsState
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.bindings.app
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.login.accounts.ui.AccountsScreen
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class AccountsActivity : ActivityGlobalAbstract() {

    @Inject
    lateinit var viewModelFactory: AccountsViewModelFactory
    private val viewModel: AccountsViewModel by viewModels { viewModelFactory }

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
                    Timber.e("Failed to load file: ", e.message.toString())
                }
                if (file.exists()) {
                    viewModel.onImportDataBase(
                        file,
                        { navigateToLogin(it) },
                        { displayMessage(ResourceManager(this, ColorUtils()).parseD2Error(it)) },
                    )
                }
            }
        }

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app().serverComponent()?.plus(AccountsModule())?.inject(this)

        setContent {
            MdcTheme {
                val accounts = viewModel.accounts.observeAsState(listOf())
                AccountsScreen(
                    accounts = accounts.value,
                    onAccountClicked = { navigateToLogin(it) },
                    onAddAccountClicked = { navigateToLogin() },
                    onImportDatabase = {
                        val intent = Intent()
                        intent.type = "*/*"
                        intent.action = Intent.ACTION_GET_CONTENT
                        filePickerLauncher.launch(intent)
                    },
                )
            }
        }
        viewModel.getAccounts()
    }

    private fun navigateToLogin(accountModel: AccountModel? = null) {
        val wasAccountClicked = accountModel?.let { true } ?: false
        val intent = LoginActivity.accountIntentResult(
            serverUrl = accountModel?.serverUrl,
            userName = accountModel?.name,
            wasAccountClicked = wasAccountClicked,
        )
        setResult(RESULT_OK, intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
    }
}
