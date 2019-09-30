package org.dhis2.usescases.about;

import org.hisp.dhis.android.core.user.UserCredentials;

/**
 * QUADRAM. Created by ppajuelo on 05/07/2018.
 */
public class AboutContracts {
    public interface AboutView{
        void renderUserCredentials(UserCredentials userCredentialsModel);
        void renderServerUrl(String serverUrl);
    }

    public interface AboutPresenter {
        void init(AboutView aboutFragment);
        void onPause();
    }
}
