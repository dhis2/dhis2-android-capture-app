package org.dhis2.utils.analytics.matomo

import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.utils.analytics.DATA_STORE_ANALYTICS_PERMISSION_KEY
import org.hisp.dhis.android.core.D2Manager
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.DownloadTracker.Extra.ApkChecksum
import org.matomo.sdk.extra.TrackHelper
import timber.log.Timber

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
        updateDhisImplementationTrackerFirstTime()
        dhisImplementationTracker?.let {
            it.userId = identification
        }
    }

    override fun trackEvent(category: String, action: String, label: String) {
        if (isAnalyticsPermissionGranted()) {
            matomoTracker?.let {
                TrackHelper.track().event(category, action).name(label).with(it)
            }

            updateDhisImplementationTrackerFirstTime()

            dhisImplementationTracker?.let {
                TrackHelper.track().event(category, action).name(label).with(it)
            }
        }
    }

    private fun isAnalyticsPermissionGranted(): Boolean {
        return (
            D2Manager.isD2Instantiated() &&
                D2Manager.getD2().dataStoreModule().localDataStore()
                .value(DATA_STORE_ANALYTICS_PERMISSION_KEY).blockingGet()?.value()
                ?.toBoolean() == true
            ).also { granted ->
            if (!granted) {
                Timber.d("Tracking is disabled")
            }
        }
    }

    private fun updateDhisImplementationTrackerFirstTime() {
        if (dhisImplementationTracker == null && D2Manager.isD2Instantiated() && D2Manager.getD2()
            .userModule().isLogged().blockingGet()
        ) {
            D2Manager.getD2().settingModule()?.let { settingModule ->
                val settings = settingModule.generalSetting().blockingGet()
                settings?.let {
                    val url = settingModule.generalSetting().blockingGet().matomoURL()
                    val id = settingModule.generalSetting().blockingGet().matomoID()
                    if (url != null && id != null) {
                        updateDhisImplementationTracker(url, id, DEFAULT_EXTERNAL_TRACKER_NAME)
                    }
                }
            }
        }
    }

    override fun trackEventWithDimension(
        category: String,
        action: String,
        label: String,
        index: Int,
        dimensionValue: String
    ) {
        if (isAnalyticsPermissionGranted()) {
            matomoTracker?.let {
                TrackHelper.track().dimension(index, dimensionValue)
                    .event(category, action).name(label).with(it)
            }
            updateDhisImplementationTrackerFirstTime()
            dhisImplementationTracker?.let {
                TrackHelper.track().dimension(index, dimensionValue)
                    .event(category, action).name(label).with(it)
            }
        }
    }

    override fun trackScreenView(screen: String, title: String) {
        if (isAnalyticsPermissionGranted()) {
            matomoTracker?.let {
                TrackHelper.track().screen(screen).title(title).with(it)
            }
            updateDhisImplementationTrackerFirstTime()
            dhisImplementationTracker?.let {
                TrackHelper.track().screen(screen).title(title).with(it)
            }
        }
    }

    override fun trackScreenViewWithDimension(
        screen: String,
        title: String,
        index: Int,
        dimensionValue: String
    ) {
        if (isAnalyticsPermissionGranted()) {
            matomoTracker?.let {
                TrackHelper.track().screen(screen).title(title)
                    .dimension(index, dimensionValue).with(it)
            }
            updateDhisImplementationTrackerFirstTime()
            dhisImplementationTracker?.let {
                TrackHelper.track().screen(screen).title(title)
                    .dimension(index, dimensionValue).with(it)
            }
        }
    }

    override fun trackScreenViewWithDimensionsAsSeparateEvents(
        screen: String,
        title: String,
        dimensions: Map<Int, String>
    ) {
        if (isAnalyticsPermissionGranted()) {
            matomoTracker?.let {
                dimensions.forEach { (key, value) ->
                    TrackHelper.track().screen(screen).title(title).dimension(key, value).with(it)
                }
            }
            updateDhisImplementationTrackerFirstTime()
            dhisImplementationTracker?.let {
                dimensions.forEach { (key, value) ->
                    TrackHelper.track().screen(screen).title(title).dimension(key, value).with(it)
                }
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
        if (isAnalyticsPermissionGranted()) {
            matomoTracker?.let {
                TrackHelper.track().screen(screen).title(title).dimension(firstIndex, firstValue)
                    .dimension(secondIndex, secondValue).with(it)
            }
            updateDhisImplementationTrackerFirstTime()
            dhisImplementationTracker?.let {
                TrackHelper.track().screen(screen).title(title).dimension(firstIndex, firstValue)
                    .dimension(secondIndex, secondValue).with(it)
            }
        }
    }

    override fun trackException(exception: Throwable, description: String) {
        if (isAnalyticsPermissionGranted()) {
            matomoTracker?.let {
                TrackHelper.track().exception(exception).description(description).with(it)
            }
            updateDhisImplementationTrackerFirstTime()
            dhisImplementationTracker?.let {
                TrackHelper.track().exception(exception).description(description).with(it)
            }
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
        if (isAnalyticsPermissionGranted()) {
            matomoTracker?.let {
                TrackHelper.track().download().identifier(apkChecksum).with(it)
            }
            updateDhisImplementationTrackerFirstTime()
            dhisImplementationTracker?.let {
                TrackHelper.track().download().identifier(apkChecksum).with(it)
            }
        }
    }

    override fun clearDhisImplementation() {
        dhisImplementationTracker = null
    }
}
