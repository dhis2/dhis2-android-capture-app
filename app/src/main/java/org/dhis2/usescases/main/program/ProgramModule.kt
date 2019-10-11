package org.dhis2.usescases.main.program

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration

import org.dhis2.R
import org.dhis2.data.dagger.PerFragment
import org.dhis2.databinding.FragmentProgramBinding
import org.hisp.dhis.android.core.D2

import dagger.Module
import dagger.Provides
import org.dhis2.data.schedulers.SchedulerProvider

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */
@Module
@PerFragment
class ProgramModule {

    @Provides
    @PerFragment
    internal fun programPresenter(homeRepository: HomeRepository, schedulerProvider : SchedulerProvider): ProgramContract.Presenter {
        return ProgramPresenter(homeRepository, schedulerProvider)
    }

    @Provides
    @PerFragment
    internal fun homeRepository(d2: D2, context: Context): HomeRepository {
        val eventsLabel = context.getString(R.string.events)
        return HomeRepositoryImpl(d2, eventsLabel)
    }

    @Provides
    @PerFragment
    internal fun providesAdapter(presenter: ProgramContract.Presenter): ProgramModelAdapter {
        return ProgramModelAdapter(presenter)
    }
}
