package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.Intent
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class TeiDataContractHandler
    @Inject
    constructor(
        registry: ActivityResultRegistry,
    ) {
        private val contractNewEventResult = MutableLiveData<Unit>()
        private val contractEditEvent = MutableLiveData<Unit>()
        private val contractScheduleEvent = MutableLiveData<Unit>()

        private val createEvent =
            registry.register(REGISTRY_NEW_EVENT, ActivityResultContracts.StartActivityForResult()) {
                contractNewEventResult.value = Unit
            }
        private val editEvent =
            registry.register(REGISTRY_EDIT_EVENT, ActivityResultContracts.StartActivityForResult()) {
                contractEditEvent.value = Unit
            }

        private val scheduleEvent =
            registry.register(REGISTRY_SCHEDULE_EVENT, ActivityResultContracts.StartActivityForResult()) {
                contractScheduleEvent.value = Unit
            }

        fun createEvent(intent: Intent): LiveData<Unit> {
            createEvent.launch(intent)
            return contractNewEventResult
        }

        fun editEvent(intent: Intent): LiveData<Unit> {
            editEvent.launch(intent)
            return contractEditEvent
        }

        fun scheduleEvent(
            intent: Intent,
            options: ActivityOptionsCompat = ActivityOptionsCompat.makeBasic(),
        ): LiveData<Unit> {
            scheduleEvent.launch(intent, options)
            return contractScheduleEvent
        }

        companion object {
            private const val REGISTRY_NEW_EVENT = "New Event"
            private const val REGISTRY_EDIT_EVENT = "Edit Event"
            private const val REGISTRY_SCHEDULE_EVENT = "Schedule Event"
        }
    }
