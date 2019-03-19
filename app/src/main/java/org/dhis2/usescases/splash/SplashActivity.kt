package org.dhis2.usescases.splash

import android.os.Bundle
import android.view.View

import org.dhis2.App
import org.dhis2.AppComponent
import org.dhis2.R
import org.dhis2.data.server.ServerComponent
import org.dhis2.databinding.ActivitySplashBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract

import javax.inject.Inject

import androidx.databinding.DataBindingUtil
import io.reactivex.functions.Consumer

class SplashActivity : ActivityGlobalAbstract(), SplashContracts.View {

    internal var binding: ActivitySplashBinding

    @Inject
    internal var presenter: SplashContracts.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val appComponent = (applicationContext as App).appComponent()
        val serverComponent = (applicationContext as App).serverComponent()
        appComponent.plus(SplashModule(serverComponent)).inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        presenter!!.init(this)
    }

    override fun onPause() {
        presenter!!.destroy()
        super.onPause()
    }

    override fun renderFlag(): Consumer<Int> {
        return { flag ->
            if (flag != -1) {
                binding.flag.setImageResource(flag!!)
                binding.logo.visibility = View.GONE
                binding.flag.visibility = View.VISIBLE
            }
            presenter!!.isUserLoggedIn()
        }
    }
}