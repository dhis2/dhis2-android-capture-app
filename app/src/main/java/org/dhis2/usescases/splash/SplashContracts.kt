package org.dhis2.usescases.splash

import androidx.annotation.UiThread
import org.dhis2.usescases.general.AbstractActivityContracts

class SplashContracts {

    interface View : AbstractActivityContracts.View {

        fun renderFlag(flagName: String)
    }

    interface Presenter {
        fun destroy()

        fun init(view: View)

        @UiThread
        fun isUserLoggedIn()

        @UiThread
        fun navigateTo(data: Class<*>)
    }
}
