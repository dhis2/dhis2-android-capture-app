/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.usescases.about

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import javax.inject.Inject
import org.dhis2.BuildConfig
import org.dhis2.Components
import org.dhis2.R
import org.dhis2.databinding.FragmentAboutBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.hisp.dhis.android.core.user.UserCredentials
import timber.log.Timber

class AboutFragment : FragmentGlobalAbstract(), AboutView {

    @Inject
    lateinit var presenter: AboutPresenter

    private lateinit var aboutBinding: FragmentAboutBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as Components).userComponent()!!
            .plus(AboutModule(this)).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        aboutBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)
        aboutBinding.presenter = presenter

        aboutBinding.aboutMore.movementMethod = LinkMovementMethod.getInstance()
        aboutBinding.aboutGit.movementMethod = LinkMovementMethod.getInstance()
        aboutBinding.aboutDev.movementMethod = LinkMovementMethod.getInstance()
        aboutBinding.aboutContact.movementMethod = LinkMovementMethod.getInstance()
        setAppVersion()
        setSDKVersion()

        return aboutBinding.root
    }

    private fun setAppVersion() {
        try {
            val versionName = abstractActivity
                .packageManager
                .getPackageInfo(abstractActivity.packageName, 0)
                .versionName

            val text = String.format(getString(R.string.about_app), versionName)
            aboutBinding.aboutApp.text = text
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }
    }

    private fun setSDKVersion() {
        val text = String.format(getString(R.string.about_sdk), BuildConfig.SDK_VERSION)
        aboutBinding.appSDK.text = text
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onPause() {
        presenter.onPause()
        super.onPause()
    }

    override fun renderUserCredentials(userCredentialsModel: UserCredentials) {
        val text = String.format(getString(R.string.about_user), userCredentialsModel.username())
        aboutBinding.aboutUser.text = text
    }

    override fun renderServerUrl(serverUrl: String?) {
        val text = String.format(getString(R.string.about_connected), serverUrl)
        aboutBinding.aboutConnected.text = text
    }
}
