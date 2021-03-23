package org.dhis2.utils.analytics.matomo

import org.hisp.dhis.android.core.D2Manager
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.DownloadTracker.Extra.ApkChecksum
import org.matomo.sdk.extra.TrackHelper

class MatomoAnalyticsControllerImpl(
    val matomoInstance: Matomo,
    val apkChecksum: ApkChecksum,
    var matomoTracker: Tracker? = TrackerController.dhis2InternalTracker(matomoInstance),
    var dhisImplementationTracker: Tracker? = null
) : MatomoAnalyticsController {

    override fun setUserId(identification: String?) {
        matomoTracker?.let {
            it.userId = identification
        }
        dhisImplementationTracker?.let {
            it.userId = identification
        }
    }

    override fun trackEvent(category: String, action: String, label: String) {
        val d2 = D2Manager.getD2()
        if (d2 != null && d2.userModule().blockingIsLogged()) {
            val userUid = d2.userModule().user().blockingGet()?.uid()
            setUserId(userUid)
        }
        matomoTracker?.let {
            TrackHelper.track().event(category, action).name(label).with(it)
        }
        dhisImplementationTracker?.let {
            TrackHelper.track().event(category, action).name(label).with(it)
        }
    }

    override fun trackEventWithDimension(
        category: String,
        action: String,
        label: String,
        index: Int,
        dimensionValue: String
    ) {
        matomoTracker?.let {
            TrackHelper.track().dimension(index, dimensionValue)
                .event(category, action).name(label).with(it)
        }
        dhisImplementationTracker?.let {
            TrackHelper.track().dimension(index, dimensionValue)
                .event(category, action).name(label).with(it)
        }
    }

    override fun trackScreenView(screen: String, title: String) {
        matomoTracker?.let {
            TrackHelper.track().screen(screen).title(title).with(it)
        }
        dhisImplementationTracker?.let {
            TrackHelper.track().screen(screen).title(title).with(it)
        }
    }

    override fun trackScreenViewWithDimension(
        screen: String,
        title: String,
        index: Int,
        dimensionValue: String
    ) {
        matomoTracker?.let {
            TrackHelper.track().screen(screen).title(title)
                .dimension(index, dimensionValue).with(it)
        }
        dhisImplementationTracker?.let {
            TrackHelper.track().screen(screen).title(title)
                .dimension(index, dimensionValue).with(it)
        }
    }

    override fun trackScreenViewWithDimensionsAsSeparateEvents(
        screen: String,
        title: String,
        dimensions: Map<Int, String>
    ) {
        matomoTracker?.let {
            dimensions.forEach { (key, value) ->
                TrackHelper.track().screen(screen).title(title).dimension(key, value).with(it)
            }
        }
        dhisImplementationTracker?.let {
            dimensions.forEach { (key, value) ->
                TrackHelper.track().screen(screen).title(title).dimension(key, value).with(it)
            }
        }
    }

    override fun trackScreenViewWithTwoDimensionsAsSameEvent(
        screen: String,
        title: String,
        firstIndex: Int,
        firstValue: String,
        secondIndex: Int,
        secondValue: String
    ) {
        matomoTracker?.let {
            TrackHelper.track().screen(screen).title(title).dimension(firstIndex, firstValue)
                .dimension(secondIndex, secondValue).with(it)
        }
        dhisImplementationTracker?.let {
            TrackHelper.track().screen(screen).title(title).dimension(firstIndex, firstValue)
                .dimension(secondIndex, secondValue).with(it)
        }
    }

    override fun trackException(exception: Throwable, description: String) {
        matomoTracker?.let {
            TrackHelper.track().exception(exception).description(description).with(it)
        }
        dhisImplementationTracker?.let {
            TrackHelper.track().exception(exception).description(description).with(it)
        }
    }

    override fun updateDefaultTracker() {
        matomoTracker = TrackerController.dhis2InternalTracker(matomoInstance)
    }

    override fun updateDhisImplementationTracker(
        matomoUrl: String,
        siteId: Int,
        trackerName: String
    ) {
        dhisImplementationTracker = TrackerController.dhis2ExternalTracker(
            matomoInstance,
            matomoUrl,
            siteId,
            trackerName
        )
    }

    override fun trackDownload() {
        matomoTracker?.let {
            TrackHelper.track().download().identifier(apkChecksum).with(it)
        }
        dhisImplementationTracker?.let {
            TrackHelper.track().download().identifier(apkChecksum).with(it)
        }
    }

    override fun clearDhisImplementation() {
        dhisImplementationTracker = null
    }
}
