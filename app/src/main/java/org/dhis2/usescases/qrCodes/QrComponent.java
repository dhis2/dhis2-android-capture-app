package org.dhis2.usescases.qrCodes;

import org.dhis2.commons.di.dagger.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = QrModule.class)
public interface QrComponent {
    void inject(QrActivity qrActivity);
}
