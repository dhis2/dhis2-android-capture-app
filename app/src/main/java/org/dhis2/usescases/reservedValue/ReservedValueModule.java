package org.dhis2.usescases.reservedValue;

import android.content.Context;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;

import dagger.Module;
import dagger.Provides;
@Module
public class ReservedValueModule {

    private Context context;

    public ReservedValueModule(Context context) {
        this.context = context;
    }

    @PerActivity
    @Provides
    ReservedValueContracts.View provideView(ReservedValueActivity activity){return activity;}

    @PerActivity
    @Provides
    ReservedValueContracts.Presenter providePresenter(ReservedValueRepository repository){
        return new ReservedValuePresenter(repository);
    }

    @PerActivity
    @Provides
    ReservedValueRepository provideRepository(BriteDatabase briteDatabase){ return new ReservedValueRepositoryImpl(briteDatabase);}
}
