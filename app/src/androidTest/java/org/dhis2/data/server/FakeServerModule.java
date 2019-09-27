package org.dhis2.data.server;

import org.hisp.dhis.android.core.D2;
import org.mockito.Mockito;

public class FakeServerModule extends ServerModule {

    @Override
    D2 sdk() {
        return Mockito.mock(D2.class);
    }
}
