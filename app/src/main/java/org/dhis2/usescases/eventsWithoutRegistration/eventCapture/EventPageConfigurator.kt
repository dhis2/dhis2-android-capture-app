package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class EventPageConfigurator(
    private val eventCaptureRepository: EventCaptureContract.EventCaptureRepository,
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
        return eventCaptureRepository.hasRelationships()
    }

    override fun displayNotes(): Boolean {
        return true
    }
}
