package com.dhis2.usescases.dataSetDetail;

import com.dhis2.data.dagger.PerActivity;

import dagger.Module;
import dagger.Provides;

/**
 * Created by frodriguez on 7/20/2018.
 */
@Module
public class DataSetDetailModule {

    @Provides
    @PerActivity
    DataSetDetailContract.View providesView(DataSetDetailActivity activity){
        return activity;
    }

    @Provides
    @PerActivity
    DataSetDetailContract.Presenter providesPresenter(){
        return new DataSetDetailPresenter();
    }
}
