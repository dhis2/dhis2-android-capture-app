package org.dhis2.usescases.qrCodes.eventsworegistration;

import org.dhis2.commons.di.dagger.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = QrEventsWORegistrationModule.class)
public interface QrEventsWORegistrationComponent {
    void inject(QrEventsWORegistrationActivity qrActivity);
}
