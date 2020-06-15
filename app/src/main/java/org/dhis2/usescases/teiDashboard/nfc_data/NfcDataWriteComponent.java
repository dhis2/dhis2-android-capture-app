package org.dhis2.usescases.teiDashboard.nfc_data;

import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 19/12/2017.
 */
@PerActivity
@Subcomponent(modules = NfcDataWriteModule.class)
public interface NfcDataWriteComponent {
    void inject(NfcDataWriteActivity activity);
}
