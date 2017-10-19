package com.dhis2.usescases.main.trackentitylist;


import com.dhis2.usescases.general.AbstractActivityContracts;
import com.evrencoskun.tableview.TableView;

import dagger.Module;
import dagger.Provides;

@Module
public class TrackEntityListContractsModule {

    @Provides
    View provideView(TrackEntityListFragment fragment) {
        return fragment;
    }

    @Provides
    TrackEntityListPresenter providesTrackEntityListPresenter(View view){
        return new TrackEntityListPresenter(view);
    }

    interface View {
        TableView setupTable();
    }

    public interface Presenter {
        void loadTrackEntities();
    }

    interface Interactor {

    }

    interface Router {

    }

}