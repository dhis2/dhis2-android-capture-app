package org.dhis2.usescases.splash

import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.metadata.MetadataRepository
import org.dhis2.data.server.ServerComponent
import org.dhis2.data.server.UserManager
import org.hisp.dhis.android.core.D2

import com.squareup.sqlbrite2.BriteDatabase

import dagger.Module
import dagger.Provides

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
class SplashModule internal constructor(serverComponent: ServerComponent?) {

    private val userManager: UserManager?

    init {
        this.userManager = serverComponent?.userManager()
    }

    @Provides
    @PerActivity
    internal fun providePresenter(splashRepository: SplashRepository): SplashContracts.Presenter {
        return SplashPresenter(userManager, splashRepository)
    }

    @Provides
    @PerActivity
    internal fun splashRepository(briteDatabase: BriteDatabase): SplashRepository {
        return SplashRepositoryImpl(briteDatabase)
    }

}
