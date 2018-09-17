package org.dhis2.usescases.teiDashboard.teiProgramList;

import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

@PerActivity
@Subcomponent(modules = TeiProgramListModule.class)
public interface TeiProgramListComponent {
    void inject(TeiProgramListActivity activity);
}