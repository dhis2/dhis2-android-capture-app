package org.dhis2.utils.analytics.matomo

import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class MatomoAnalyticsControllerImpl(val matomoTracker: Tracker?) :
    MatomoAnalyticsController {

    override fun trackEvent(category: String, action: String, label: String) {
        if (matomoTracker == null) return
        TrackHelper.track().event(category, action).name(label).with(matomoTracker)
    }

    override fun trackScreenView(screen: String, title: String) {
        if (matomoTracker == null) return
        TrackHelper.track().screen(screen).title(title).with(matomoTracker)
    }
}