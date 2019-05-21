package org.dhis2.utils;

import androidx.annotation.NonNull;

/**
 * QUADRAM. Created by ppajuelo on 21/03/2019.
 */
public class TestingCredential {

    @NonNull
    private String server_url;
    @NonNull
    private String user_name;
    @NonNull
    private String user_pass;

    @NonNull
    public String getServerUrl() {
        return server_url;
    }

    @NonNull
    public String getUserName() {
        return user_name;
    }

    @NonNull
    public String getUserPass() {
        return user_pass;
    }
}
