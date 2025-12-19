package org.dhis2.usescases.settings

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.data.FormFileProvider
import org.dhis2.commons.data.FormFileProvider.init
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.mobile.login.authentication.TwoFASettingsActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.reservedValue.ReservedValueActivity
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.ui.SettingsScreen
import org.dhis2.usescases.settingsprogram.SettingsProgramActivity
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class SyncManagerFragment : FragmentGlobalAbstract() {
    @Inject
    lateinit var settingsViewModelFactory: SettingsViewModelFactory

    private val presenter: SyncManagerPresenter by viewModels { settingsViewModelFactory }

    @JvmField
    @Inject
    var colorUtils: ColorUtils? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        app()
            .userComponent()
            ?.plus(SyncManagerModule())
            ?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        init(requireContext())
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                DHIS2Theme {
                    SettingsScreen(
                        viewmodel = presenter,
                        checkProgramSpecificSettings = {
                            startActivity(
                                SettingsProgramActivity.Companion.getIntentActivity(
                                    requireContext(),
                                ),
                            )
                        },
                        manageReserveValues = {
                            startActivity(
                                ReservedValueActivity::class.java,
                                null,
                                false,
                                false,
                                null,
                            )
                        },
                        showErrorLogs = ::showSyncErrors,
                        showShareActions = ::shareDB,
                        display2FASettingsScreen = ::display2FASettingsScreen,
                    )
                }
            }
        }
    }

    override fun onStop() {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(123456)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.closeChannel()
    }

    private fun showSyncErrors(data: List<ErrorViewModel>) {
        ErrorDialog()
            .setData(data)
            .show(getChildFragmentManager().beginTransaction(), ErrorDialog.TAG)
    }

    private fun shareDB(fileToShare: File) {
        val contentUri =
            FileProvider.getUriForFile(
                requireContext(),
                FormFileProvider.fileProviderAuthority,
                fileToShare,
            )
        val intentShare =
            Intent(Intent.ACTION_SEND)
                .setDataAndType(
                    contentUri,
                    requireContext().contentResolver.getType(contentUri),
                ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .putExtra(Intent.EXTRA_STREAM, contentUri)
        val chooser = Intent.createChooser(intentShare, getString(R.string.open_with))
        try {
            startActivity(chooser)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun display2FASettingsScreen() {
        startActivity(
            Intent(requireContext(), TwoFASettingsActivity::class.java),
        )
    }
}
