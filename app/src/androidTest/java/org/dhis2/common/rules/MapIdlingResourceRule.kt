package org.dhis2.common.rules

import androidx.test.espresso.IdlingRegistry
import androidx.test.rule.ActivityTestRule
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.dhis2.common.idlingresources.MapIdlingResource
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MapIdlingResourceRule(
    activityTestRule: ActivityTestRule<*>
) : TestWatcher() {

    private val idlingResource = MapIdlingResource(activityTestRule)
    var map: MapboxMap? = idlingResource.getMap()

    override fun finished(description: Description?) {
        IdlingRegistry.getInstance().unregister(idlingResource)
        super.finished(description)
    }

    override fun starting(description: Description?) {
        IdlingRegistry.getInstance().register(idlingResource)
        super.starting(description)
    }
}
