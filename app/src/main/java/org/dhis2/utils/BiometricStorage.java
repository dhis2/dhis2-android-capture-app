package org.dhis2.utils;

import de.adorsys.android.securestoragelibrary.SecurePreferences;

/**
 * QUADRAM. Created by ppajuelo on 24/01/2019.
 */
public class BiometricStorage {

    public static void saveUserCredentials(String serverUrl, String userName, String pass) {
        SecurePreferences.setValue(Constants.SECURE_CREDENTIALS, true);
        SecurePreferences.setValue(Constants.SECURE_SERVER_URL, serverUrl);
        SecurePreferences.setValue(Constants.SECURE_USER_NAME, userName);
        SecurePreferences.setValue(Constants.SECURE_PASS, pass);
    }

    public static Boolean areCredentialsSet() {
        return SecurePreferences.getBooleanValue(Constants.SECURE_CREDENTIALS, false);
    }

    public static Boolean areSameCredentials(String serverUrl, String userName, String pass) {
        return SecurePreferences.getStringValue(Constants.SECURE_SERVER_URL, "").equals(serverUrl) &&
                SecurePreferences.getStringValue(Constants.SECURE_USER_NAME, "").equals(userName) &&
                SecurePreferences.getStringValue(Constants.SECURE_PASS, "").equals(pass);
    }

}
