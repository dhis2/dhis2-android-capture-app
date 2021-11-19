package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class EventPageConfigurator(
    private val eventCaptureRepository: EventCaptureContract.EventCaptureRepository,
    private val featureConfig: FeatureConfigRepository
) : NavigationPageConfigurator {
    override fun displayDetails(): Boolean {
        return true
    }

    override fun displayDataEntry(): Boolean {
        return true
    }

    override fun displayAnalytics(): Boolean {
        return eventCaptureRepository.hasAnalytics()
    }

    override fun displayRelationships(): Boolean {
        return eventCaptureRepository.hasRelationships() &&
            featureConfig.isFeatureEnable(Feature.ANDROAPP_2275)
    }

    override fun displayNotes(): Boolean {
        return true
    }
}
