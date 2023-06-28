package org.dhis2.usescases.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.Bindings.buildInfo
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.databinding.FragmentAboutBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.hisp.dhis.android.core.user.User

class AboutFragment : FragmentGlobalAbstract(), AboutView {

    @Inject
    lateinit var presenter: AboutPresenter
    private lateinit var aboutBinding: FragmentAboutBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        app().userComponent()?.plus(AboutModule(this))?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        aboutBinding = FragmentAboutBinding.inflate(inflater, container, false)
        return aboutBinding.apply {
            aboutMore.movementMethod
            aboutMore.movementMethod = LinkMovementMethod.getInstance()
            aboutGit.movementMethod = LinkMovementMethod.getInstance()
            aboutDev.movementMethod = LinkMovementMethod.getInstance()
            aboutContact.movementMethod = LinkMovementMethod.getInstance()
            aboutApp.text = getString(R.string.about_app).format(context?.buildInfo())
            appSDK.text = getString(R.string.about_sdk).format(BuildConfig.SDK_VERSION)
            privacyPolicy.setOnClickListener { navigateToPrivacyPolicy() }
        }.root
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onPause() {
        presenter.onPause()
        super.onPause()
    }

    override fun renderUserCredentials(userModel: User?) {
        aboutBinding.aboutUser.text =
            getString(R.string.about_user).format(userModel?.username())
    }

    override fun renderServerUrl(serverUrl: String?) {
        aboutBinding.aboutConnected.text = getString(R.string.about_connected).format(serverUrl)
    }

    override fun navigateToPrivacyPolicy() {
        activity?.let {
            startActivity(Intent(it, PolicyView::class.java))
        }
    }
}
