// TODO: review EyeSeeTea
/*package org.dhis2.data.notifications

import org.dhis2.usescases.notifications.domain.Notification
import org.dhis2.usescases.notifications.domain.UserGroups
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationsApi {
    @GET("dataStore/notifications/notifications")
    fun getData(): Call<List<Notification>>

    @PUT("dataStore/notifications/notifications")
    fun postData(@Body notifications:List<Notification>): Call<Unit>
}

interface UserGroupsApi {
    @GET("users/{userId}?fields=userGroups")
    fun getData(@Path("userId") userId:String): Call<UserGroups>
}*/

/*
class NotificationsApi (private val client: HttpServiceClient) {
    suspend fun getData(): List<Notification>{
        return client.get {
            url("dataStore/notifications/notifications")
        }
    }

    suspend fun postData( notifications:List<Notification>): User {
        return client.put {
            url("dataStore/notifications/notifications")
            body(notifications)
        }
    }
}

class UserGroupsApi (private val client: HttpServiceClient) {
    suspend fun getData( userId:String): UserGroups {
        return client.get {
            url("users/$userId?fields=userGroups")
        }
    }
}
 */
