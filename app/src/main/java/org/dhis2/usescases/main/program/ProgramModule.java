package org.dhis2.usescases.main.program;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;

import org.dhis2.R;
import org.dhis2.data.dagger.PerFragment;
import org.dhis2.databinding.FragmentProgramBinding;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */
@Module
@PerFragment
public class ProgramModule {

    @Provides
    @PerFragment
    ProgramContract.Presenter programPresenter(HomeRepository homeRepository) {
        return new ProgramPresenter(homeRepository);
    }

    @Provides
    @PerFragment
    HomeRepository homeRepository(D2 d2, Context context) {
        String eventsLabel = context.getString(R.string.events);
        return new HomeRepositoryImpl(d2, eventsLabel);
    }

    @Provides
    @PerFragment
    ProgramModelAdapter providesAdapter(ProgramContract.Presenter presenter){
       return new ProgramModelAdapter(presenter);
    }
}
