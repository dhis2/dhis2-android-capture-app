package org.dhis2.data.server;

import androidx.test.core.app.ApplicationProvider;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.common.collect.Lists;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.d2manager.D2Configuration;
import org.hisp.dhis.android.core.d2manager.D2Manager;

public class FakeServerModule extends ServerModule {

    @Override
    D2 sdk() {
        D2Manager.setUp(getD2Configuration()).blockingAwait();
        D2Manager.setServerUrl("https://play.dhis2.org/android-current/").andThen(D2Manager.instantiateD2()).blockingGet();
        return D2Manager.getD2();
    }

    private static D2Configuration getD2Configuration() {
        return D2Configuration.builder()
                .appName("app_name")
                .appVersion("1.0.0")
                .networkInterceptors(Lists.newArrayList(new StethoInterceptor()))
                .context(ApplicationProvider.getApplicationContext())
                .build();
    }
}
