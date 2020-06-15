package org.dhis2.usescases.sms;

import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = SmsModule.class)
public interface SmsComponent {
    void inject(SmsSendingService sendingService);
}
