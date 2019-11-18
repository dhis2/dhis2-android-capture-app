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

package org.dhis2.usescases.sync

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.airbnb.lottie.LottieDrawable
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.Bindings.Bindings
import org.dhis2.R
import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.databinding.ActivitySynchronizationBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.Constants

class SyncActivity : ActivityGlobalAbstract(), SyncView {

    lateinit var binding: ActivitySynchronizationBinding

    @Inject
    lateinit var presenter: SyncPresenter

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var preferences: PreferenceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as App).userComponent()!!.plus(SyncModule(this)).inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_synchronization)
        binding.presenter = presenter
        presenter.sync()
    }

    override fun onResume() {
        super.onResume()
        workManager.getWorkInfosForUniqueWorkLiveData(Constants.INITIAL_SYNC)
            .observe(
                this,
                Observer { workInfoList ->
                    for (wi in workInfoList) {
                        if (wi.tags.contains(Constants.META_NOW)) {
                            handleMetaState(wi.state)
                        } else if (wi.tags.contains(Constants.DATA_NOW)) {
                            handleDataState(wi.state)
                        }
                    }
                }
            )
    }

    private fun handleMetaState(metadataState: WorkInfo.State) {
        when (metadataState) {
            WorkInfo.State.RUNNING -> Bindings.setDrawableEnd(
                binding.metadataText,
                AppCompatResources.getDrawable(this, R.drawable.animator_sync)
            )
            WorkInfo.State.SUCCEEDED -> {
                binding.metadataText.text = getString(R.string.configuration_ready)
                Bindings.setDrawableEnd(
                    binding.metadataText,
                    AppCompatResources.getDrawable(this, R.drawable.animator_done)
                )
                presenter.getTheme()
            }
            else -> {
            }
        }
    }

    private fun handleDataState(dataState: WorkInfo.State) {
        when (dataState) {
            WorkInfo.State.RUNNING -> {
                binding.eventsText.text = getString(R.string.syncing_data)
                Bindings.setDrawableEnd(
                    binding.eventsText,
                    AppCompatResources.getDrawable(this, R.drawable.animator_sync)
                )
                binding.eventsText.alpha = 1.0f
            }
            WorkInfo.State.SUCCEEDED -> {
                binding.eventsText.text = getString(R.string.data_ready)
                Bindings.setDrawableEnd(
                    binding.eventsText,
                    AppCompatResources.getDrawable(this, R.drawable.animator_done)
                )
                presenter.syncReservedValues()
                startMain()
            }
            else -> {
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (binding.lottieView != null) {
            binding.lottieView.run {
                repeatCount = LottieDrawable.INFINITE
                repeatMode = LottieDrawable.RESTART
                useHardwareAcceleration(true)
                enableMergePathsForKitKatAndAbove(true)
                binding.lottieView.playAnimation()
            }
        }
    }

    override fun onStop() {
        if (binding.lottieView != null) {
            binding.lottieView.cancelAnimation()
        }
        presenter.onDettach()
        super.onStop()
    }

    override fun saveTheme(themeColor: String) {
        val style = when {
            themeColor.contains("green") -> R.style.GreenTheme
            themeColor.contains("india") -> R.style.OrangeTheme
            themeColor.contains("myanmar") -> R.style.RedTheme
            else -> R.style.AppTheme
        }

        preferences.setValue(Preference.THEME, style)
        setTheme(style)

        val startColor = ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY)
        val typedValue = TypedValue()
        val a = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
        val endColor = a.getColor(0, 0)
        a.recycle()

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        colorAnimation.duration = 2000 // milliseconds
        colorAnimation.addUpdateListener {
            binding.logo.setBackgroundColor(it.animatedValue as Int)
        }
        colorAnimation.start()
    }

    override fun saveFlag(flag: String) {
        preferences.setValue(Preference.FLAG, flag)

        binding.logoFlag.setImageResource(resources.getIdentifier(flag, "drawable", packageName))
        val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
        alphaAnimator.duration = 2000
        alphaAnimator.addUpdateListener {
            binding.logoFlag.alpha = it.animatedValue as Float
            binding.dhisLogo.alpha = 0.toFloat()
        }
        alphaAnimator.start()
    }

    private fun startMain() {
        preferences.setValue(Preference.INITIAL_SYNC_DONE, true)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
