package org.dhis2.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * QUADRAM. Created by ppajuelo on 21/03/2019.
 */
public class TestingCredential {

    @NonNull
    private String serverUrl;
    @NonNull
    private String userName;
    @NonNull
    private String userPass;
    @Nullable
    private String serverVersion;

    @NonNull
    public String getServerUrl() {
        return serverUrl;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    @NonNull
    public String getUserPass() {
        return userPass;
    }
}
