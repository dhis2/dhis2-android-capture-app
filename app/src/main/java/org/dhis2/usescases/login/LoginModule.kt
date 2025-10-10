package org.dhis2.usescases.login

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.general.ActivityGlobalAbstract

@Module
class LoginModule(
    private val view: LoginContracts.View,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val userManager: UserManager?,
) {
    @Provides
    fun provideActivity(): ActivityGlobalAbstract = view.abstractActivity

    @Provides
    @PerActivity
    fun provideResourceManager(colorUtils: ColorUtils) = ResourceManager(view.context, colorUtils)

    @Provides
    @PerActivity
    fun providePresenter(resourceManager: ResourceManager): LoginViewModel =
        ViewModelProvider(
            viewModelStoreOwner,
            LoginViewModelFactory(
                view,
                resourceManager,
                userManager,
            ),
        )[LoginViewModel::class.java]
}
