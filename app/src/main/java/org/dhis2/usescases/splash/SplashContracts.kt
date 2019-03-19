package org.dhis2.usescases.splash

import androidx.annotation.UiThread

import org.dhis2.usescases.general.AbstractActivityContracts

import io.reactivex.functions.Consumer

class SplashContracts {

    internal interface View : AbstractActivityContracts.View {

        fun renderFlag(): Consumer<Int>
    }

    interface Presenter {
        fun destroy()

        fun init(view: View)

        @UiThread
        fun isUserLoggedIn()

        @UiThread
        fun navigateToLoginView()

        @UiThread
        fun navigateToHomeView()
    }
}