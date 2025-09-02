package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class EventPageConfigurator(
    private val eventCaptureRepository: EventCaptureContract.EventCaptureRepository,
    val isPortrait: Boolean,
) : NavigationPageConfigurator {
    override fun displayDetails(): Boolean = true

    override fun displayDataEntry(): Boolean = isPortrait

    override fun displayAnalytics(): Boolean = eventCaptureRepository.hasAnalytics()

    override fun displayRelationships(): Boolean = eventCaptureRepository.hasRelationships()

    override fun displayNotes(): Boolean = true
}
