package org.dhis2.usescases.teiDashboard;
import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = TeiDashboardModule.class)
public interface TeiDashboardComponentFlavor extends TeiDashboardComponent{}
