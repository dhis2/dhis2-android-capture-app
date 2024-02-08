package org.dhis2.data.notifications

import org.dhis2.usescases.notifications.domain.Notification
import org.dhis2.usescases.notifications.domain.UserGroups
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface NotificationsApi {
    @GET("dataStore/notifications/notifications")
    fun getData(): Call<List<Notification>>
}

interface UserGroupsApi {
    @GET("users/{userId}?fields=userGroups")
    fun getData(@Path("userId") userId:String): Call<UserGroups>
}
