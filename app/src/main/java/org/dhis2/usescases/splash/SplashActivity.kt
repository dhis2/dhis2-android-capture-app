package org.dhis2.usescases.splash

import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import android.os.Bundle
import android.os.Debug
import android.text.TextUtils.isEmpty
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.scottyab.rootbeer.RootBeer
import org.dhis2.App
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.databinding.ActivitySplashBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.sync.SyncActivity
import javax.inject.Inject
import javax.inject.Named

class SplashActivity : ActivityGlobalAbstract(), SplashView {
    companion object {
        const val FLAG = "FLAG"
    }

    lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var presenter: SplashPresenter

    @Inject
    @field:Named(FLAG)
    lateinit var flag: String

    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        val appComponent = (applicationContext as App).appComponent()
        val serverComponent = (applicationContext as App).serverComponent()
        appComponent.plus(SplashModule(this, serverComponent)).inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        renderFlag(flag)
    }

    override fun onResume() {
        super.onResume()

        if (BuildConfig.DEBUG || !RootBeer(this).isRootedWithoutBusyBoxCheck) {
            if (!isDebuggerEnable() || !detectDebugger()) {
                presenter.init()
            } else {
                showRootedDialog(
                    getString(R.string.security_title),
                    getString(R.string.security_debugger_message),
                )
            }
        } else {
            showRootedDialog(
                getString(R.string.security_title),
                getString(R.string.security_rooted_message),
            )
        }
    }

    override fun onPause() {
        presenter.destroy()
        super.onPause()
    }

    override fun renderFlag(flagName: String) {
        val resource = if (!isEmpty(flagName)) {
            resources.getIdentifier(flagName, "drawable", packageName)
        } else {
            -1
        }
        if (resource != -1) {
            binding.flag.setImageResource(resource)
            binding.logo.visibility = View.GONE
            binding.flag.visibility = View.VISIBLE
        }
    }

    private fun showRootedDialog(title: String, message: String) {
        alertDialog = MaterialAlertDialogBuilder(activity, R.style.MaterialDialog).create()
        if (!alertDialog.isShowing) {
            // TITLE
            val titleView =
                LayoutInflater.from(activity).inflate(R.layout.dialog_rooted_title, null)
            titleView.findViewById<TextView>(R.id.dialogTitle).text = title
            alertDialog.setCustomTitle(titleView)

            // BODY
            val msgView = LayoutInflater.from(activity).inflate(R.layout.dialog_rooted_body, null)
            msgView.findViewById<TextView>(R.id.dialogBody).text = message

            msgView.findViewById<Button>(R.id.dialogOk).setOnClickListener {
                alertDialog.dismiss()
                finish()
            }
            alertDialog.setView(msgView)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    override fun goToNextScreen(
        isUserLogged: Boolean,
        sessionLocked: Boolean,
        initialSyncDone: Boolean,
        initialDataSyncDone: Boolean,
    ) {
        if (isUserLogged && initialSyncDone && !sessionLocked) {
            startActivity(
                MainActivity::class.java,
                MainActivity.bundle(launchDataSync = initialDataSyncDone),
                true,
                true,
                null,
            )
        } else if (isUserLogged && !initialSyncDone) {
            startActivity(
                SyncActivity::class.java,
                null,
                true,
                true,
                null,
            )
        } else {
            startActivity(
                LoginActivity::class.java,
                LoginActivity.bundle(
                    accountsCount = presenter.getAccounts(),
                ),
                true,
                true,
                null,
            )
        }
    }

    private fun isDebuggerEnable(): Boolean {
        return if (!BuildConfig.DEBUG) {
            context.applicationContext.applicationInfo.flags and FLAG_DEBUGGABLE != 0
        } else {
            false
        }
    }

    private fun detectDebugger(): Boolean {
        return if (!BuildConfig.DEBUG) {
            Debug.isDebuggerConnected()
        } else {
            false
        }
    }
}
