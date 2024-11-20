package org.dhis2.usescases.pushnotifications

import android.util.Log
import androidx.annotation.Keep
import com.google.gson.Gson
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ApiService {
    private val gson = Gson()

    private val subscriberClient = OkHttpClient.Builder()
        .readTimeout(77, TimeUnit.SECONDS) // Assuming that keepalive messages are more frequent than this
        .build()


    fun subscribe(
        baseUrl: String,
        topics: String,
        since: Long,
        notify: (topic: String, Notification) -> Unit,
        fail: (Exception) -> Unit
    ): Call {
        val sinceVal = if (since == 0L) "all" else since.toString()
        val url = topicUrlJson(baseUrl, topics, sinceVal)
        Log.d(TAG, "Opening subscription connection to $url")

        val request = Request.Builder().url(url).build()
        val call = subscriberClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        throw Exception("Unexpected response ${response.code} when subscribing to topic $url")
                    }
                    val source = response.body?.source() ?: throw Exception("Unexpected response for $url: body is empty")
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: throw Exception("Unexpected response for $url: line is null")
                        val message = gson.fromJson(line, Message::class.java)
                        if (message.event == EVENT_MESSAGE) {
                            val topic = message.topic
                            val notification = Notification(
                                id = message.id,
                                subscriptionId = 0, // TO BE SET downstream
                                timestamp = message.time,
                                message = message.message,
                                notificationId = Random.nextInt(),
                                deleted = false
                            )
                            notify(topic, notification)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Connection to $url failed (1): ${e.message}", e)
                    fail(e)
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Connection to $url failed (2): ${e.message}", e)
                fail(e)
            }
        })
        return call
    }


    /* This annotation ensures that proguard still works in production builds,
     * see https://stackoverflow.com/a/62753300/1440785 */
    @Keep
    private data class Message(
        val id: String,
        val time: Long,
        val event: String,
        val topic: String,
        val message: String
    )

    companion object {
        private const val TAG = "NtfyApiService"
        private const val EVENT_MESSAGE = "message"
    }
}

data class Notification(
    val id: String,
    val subscriptionId: Long,
    val timestamp: Long, // Unix timestamp
    val message: String,
    val notificationId: Int, // Android notification popup ID
    val deleted: Boolean,
)

data class Subscription(
    val id: Long, // Internal ID, only used in Repository and activities
    val baseUrl: String,
    val topic: String,
    val instant: Boolean,
    val mutedUntil: Long, // TODO notificationSound, notificationSchedule
    val totalCount: Int = 0, // Total notifications
    val newCount: Int = 0, // New notifications
    val lastActive: Long = 0, // Unix timestamp
    val state: ConnectionState = ConnectionState.NOT_APPLICABLE
)

enum class ConnectionState {
    NOT_APPLICABLE, CONNECTING, CONNECTED
}

fun topicUrl(baseUrl: String, topic: String) = "${baseUrl}/${topic}"
fun topicUrlJson(baseUrl: String, topic: String, since: String) = "${topicUrl(baseUrl, topic)}/json?since=$since"
fun topicShortUrl(baseUrl: String, topic: String) =
    topicUrl(baseUrl, topic)
        .replace("http://", "")
        .replace("https://", "")
