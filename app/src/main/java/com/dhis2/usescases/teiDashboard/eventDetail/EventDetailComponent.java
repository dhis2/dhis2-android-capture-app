package com.dhis2.usescases.teiDashboard.eventDetail;

import com.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by ppajuelo on 19/12/2017.
 */
@PerActivity
@Subcomponent(modules = EventDetailModule.class)
public interface EventDetailComponent {
    void inject(EventDetailActivity activity);
}
