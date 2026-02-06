package org.dhis2.usescases.sync

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.dhis2.App
import org.dhis2.R
import org.dhis2.bindings.Bindings
import org.dhis2.bindings.userComponent
import org.dhis2.databinding.ActivitySynchronizationBinding
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.navigateTo
import org.dhis2.utils.OnDialogClickListener
import org.dhis2.utils.extension.navigateTo
import org.dhis2.utils.extension.share
import org.koin.android.ext.android.inject
import javax.inject.Inject

class SyncActivity :
    ActivityGlobalAbstract(),
    SyncView {
    lateinit var binding: ActivitySynchronizationBinding

    @Inject
    lateinit var presenter: SyncPresenter

    @Inject
    lateinit var animations: SyncAnimations

    private val backgroundJobAction: SyncBackgroundJobAction by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val serverComponent = (applicationContext as App).serverComponent()
        userComponent()?.plus(SyncModule(this, backgroundJobAction, serverComponent))?.inject(this)
            ?: finish()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_synchronization)
        binding.presenter = presenter
        presenter.sync()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            presenter.observeSyncProcess().collect(presenter::handleSyncInfo)
        }
    }

    override fun setMetadataSyncStarted() {
        Bindings.setDrawableEnd(
            binding.metadataText,
            AppCompatResources.getDrawable(
                this,
                R.drawable.animator_sync,
            ),
        )
    }

    override fun setMetadataSyncSucceed() {
        binding.metadataText.text = getString(R.string.configuration_ready)
        Bindings.setDrawableEnd(
            binding.metadataText,
            AppCompatResources.getDrawable(
                this,
                R.drawable.animator_done,
            ),
        )
        presenter.onMetadataSyncSuccess()
    }

    override fun showMetadataFailedMessage(message: String?) {
        showInfoDialog(
            getString(R.string.something_wrong),
            getString(R.string.metada_first_sync_error),
            getString(R.string.share),
            getString(R.string.go_back),
            object : OnDialogClickListener {
                override fun onPositiveClick() {
                    message?.let { share(it) }
                }

                override fun onNegativeClick() {
                    presenter.onLogout()
                }
            },
        )
    }

    override fun onStart() {
        super.onStart()
        animations.startLottieAnimation(binding.lottieView)
    }

    override fun onStop() {
        binding.lottieView.cancelAnimation()
        presenter.onDetach()
        super.onStop()
    }

    override fun setServerTheme(themeId: Int) {
        animations.startThemeAnimation(this, { super.setTheme(themeId) }) { colorValue ->
            binding.logo.setBackgroundColor(colorValue)
        }
    }

    override fun setFlag(flagName: String?) {
        flagName?.takeIf { it.isNotBlank() }?.let {
            val flagRes = resources.getIdentifier(flagName, "drawable", packageName)
            binding.logoFlag.setImageResource(flagRes)
            animations.startFlagAnimation { value ->
                binding.apply {
                    logoFlag.alpha = value
                    dhisLogo.alpha = 0f
                }
            }
        } ?: run {
            // Hide flag if no valid name provided
            binding.logoFlag.visibility = GONE
        }
    }

    override fun goToMain() {
        startActivity(
            MainActivity.intent(this, launchDataSync = true).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
        finish()
    }

    override fun goToLogin() {
        navigateTo<LoginActivity>(true, flagsToApply = Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
