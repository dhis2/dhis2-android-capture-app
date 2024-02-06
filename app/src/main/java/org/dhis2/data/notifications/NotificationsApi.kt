package org.dhis2.data.notifications

import org.dhis2.usescases.notifications.domain.Notification
import retrofit2.Call
import retrofit2.http.GET

interface NotificationsApi {
    @GET("dataStore/notifications/notifications")
    fun getData(): Call<List<Notification>>
}
