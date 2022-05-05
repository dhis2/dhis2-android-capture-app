package org.dhis2.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 16/04/2018.
 */
@Deprecated
public class NetworkUtils {

    /**
     * Check if network available or not
     *
     * @param context app context
     */
    public static boolean isOnline(Context context) {
        boolean isOnline = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                //should check null because in airplane mode it will be null
                isOnline = (netInfo != null && netInfo.isConnectedOrConnecting());
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return isOnline;
    }
}
