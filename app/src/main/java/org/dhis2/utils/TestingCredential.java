package org.dhis2.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    @Nullable
    private String server_version;

    @NonNull
    public String getServer_url() {
        return server_url;
    }

    @NonNull
    public String getUser_name() {
        return user_name;
    }

    @NonNull
    public String getUser_pass() {
        return user_pass;
    }
}
