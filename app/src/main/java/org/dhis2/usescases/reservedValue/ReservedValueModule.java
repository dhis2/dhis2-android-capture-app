package org.dhis2.usescases.reservedValue;

import android.content.Context;

import org.dhis2.R;
import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

@Module
public class ReservedValueModule {

    private ReservedValueView view;

    ReservedValueModule(ReservedValueActivity view) {
        this.view = view;
    }

    @PerActivity
    @Provides
    ReservedValuePresenter providePresenter(ReservedValueRepository repository, SchedulerProvider schedulerProvider, FlowableProcessor<String> refillProcessor) {
        return new ReservedValuePresenter(repository, schedulerProvider, view, refillProcessor);
    }

    @PerActivity
    @Provides
    ReservedValueRepository provideRepository(D2 d2, ReservedValueMapper mapper, PreferenceProvider preferenceProvider) {
        return new ReservedValueRepositoryImpl(d2, preferenceProvider, mapper);
    }

    @PerActivity
    @Provides
    FlowableProcessor<String> refillProcessor() {
        return PublishProcessor.create();
    }

    @PerActivity
    @Provides
    ReservedValueMapper reservedValueMapper(Context context, FlowableProcessor<String> refillProcessor) {
        return new ReservedValueMapper(refillProcessor, context.getString(R.string.reserved_values_left));
    }
}
