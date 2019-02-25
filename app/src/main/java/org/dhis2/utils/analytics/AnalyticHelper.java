package org.dhis2.utils.analytics;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.EnumMap;
import java.util.Map;

/**
 * QUADRAM. Created by ppajuelo on 21/01/2019.
 */
public class AnalyticHelper {

    private static String propertyId;

    private static Map<TrackerName, Tracker> mTrackers = new EnumMap<>(TrackerName.class);


    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    public static AnalyticHelper init(String propertyID) { //store in Application
        AnalyticHelper helper = new AnalyticHelper();
        propertyId = propertyID;
        return helper;
    }

    public static synchronized Tracker getTracker(Context applicationContext, TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(applicationContext);
            Tracker t = analytics.newTracker(propertyId);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    public Map<String, String> sendScreen() {
        return new HitBuilders.ScreenViewBuilder()
                .build();
    }

    public Map<String, String> sendEvent(String category, String action, String label, long value) {
        return new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build();
    }

}
