package org.dhis2.usescases.qrReader;

import org.dhis2.commons.di.dagger.PerFragment;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = QrReaderModule.class)
public interface QrReaderComponent {
    void inject(QrReaderFragment qrReaderFragment);

}
