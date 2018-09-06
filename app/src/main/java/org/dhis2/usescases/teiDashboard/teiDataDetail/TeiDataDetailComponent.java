package org.dhis2.usescases.teiDashboard.teiDataDetail;

import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by frodriguez on 12/13/2017.
 */
@PerActivity
@Subcomponent(modules = TeiDataDetailModule.class)
public interface TeiDataDetailComponent {

    void inject(TeiDataDetailActivity detailActivity);
}
