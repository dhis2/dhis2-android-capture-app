package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class EventPageConfigurator : NavigationPageConfigurator {
    override fun displayDetails(): Boolean {
        return true
    }

    override fun displayDataEntry(): Boolean {
        return true
    }

    override fun displayAnalytics(): Boolean {
        return true
    }

    override fun displayNotes(): Boolean {
        return true
    }
}
