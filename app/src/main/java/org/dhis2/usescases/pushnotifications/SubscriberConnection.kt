package org.dhis2.usescases.pushnotifications

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.Call
import java.util.concurrent.atomic.AtomicBoolean

class SubscriberConnection(
    private val api: ApiService,
    private val baseUrl: String,
    private val sinceTime: Long,
    private val subscriptions: Map<Long, Subscription>,
    private val stateChangeListener: (Collection<Subscription>, ConnectionState) -> Unit,
    private val notificationListener: (Subscription, Notification) -> Unit,
    private val serviceActive: () -> Boolean
) {
    private val topicsStr = subscriptions.values.joinToString(separator = ",") { s -> s.topic }
    private val url = topicUrl(baseUrl, topicsStr)

    private var since: Long = sinceTime
    private lateinit var call: Call
    private lateinit var job: Job

    fun start(scope: CoroutineScope) {
        job = scope.launch(Dispatchers.IO) {
            // Retry-loop: if the connection fails, we retry unless the job or service is cancelled/stopped
            var retryMillis = 0L
            while (isActive && serviceActive()) {
                val startTime = System.currentTimeMillis()
                val notify = { topic: String, notification: Notification ->
                    since = notification.timestamp
                    val subscription = subscriptions.values.first { it.topic == topic }
                    val notificationWithSubscriptionId = notification.copy(subscriptionId = subscription.id)
                    notificationListener(subscription, notificationWithSubscriptionId)
                }
                val failed = AtomicBoolean(false)
                val fail = { e: Exception ->
                    failed.set(true)
                    if (isActive && serviceActive()) { // Avoid UI update races if we're restarting a connection
                        stateChangeListener(subscriptions.values, ConnectionState.CONNECTING)
                    }
                }

                // Call /json subscribe endpoint and loop until the call fails, is canceled,
                // or the job or service are cancelled/stopped
                try {
                    call = api.subscribe(baseUrl, topicsStr, since, notify, fail)
                    while (!failed.get() && !call.isCanceled() && isActive && serviceActive()) {
                        stateChangeListener(subscriptions.values, ConnectionState.CONNECTED)
                        Log.d(TAG,"[$url] Connection is active (failed=$failed, callCanceled=${call.isCanceled()}, jobActive=$isActive, serviceStarted=${serviceActive()}")
                        delay(CONNECTION_LOOP_DELAY_MILLIS) // Resumes immediately if job is cancelled
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[$url] Connection failed: ${e.message}", e)
                    if (isActive && serviceActive()) { // Avoid UI update races if we're restarting a connection
                        stateChangeListener(subscriptions.values, ConnectionState.CONNECTING)
                    }
                }

                // If we're not cancelled yet, wait little before retrying (incremental back-off)
                if (isActive && serviceActive()) {
                    retryMillis = nextRetryMillis(retryMillis, startTime)
                    Log.d(TAG, "[$url] Connection failed, retrying connection in ${retryMillis / 1000}s ...")
                    delay(retryMillis)
                }
            }
            Log.d(TAG, "[$url] Connection job SHUT DOWN")
        }
    }

    fun matches(otherSubscriptions: Map<Long, Subscription>): Boolean {
        return subscriptions.keys == otherSubscriptions.keys
    }

    fun since(): Long {
        return since
    }

    fun cancel() {
        Log.d(TAG, "[$url] Cancelling connection")
        job?.cancel()
        call?.cancel()
    }

    private fun nextRetryMillis(retryMillis: Long, startTime: Long): Long {
        val connectionDurationMillis = System.currentTimeMillis() - startTime
        if (connectionDurationMillis > RETRY_RESET_AFTER_MILLIS) {
            return RETRY_STEP_MILLIS
        } else if (retryMillis + RETRY_STEP_MILLIS >= RETRY_MAX_MILLIS) {
            return RETRY_MAX_MILLIS
        }
        return retryMillis + RETRY_STEP_MILLIS
    }

    companion object {
        private const val TAG = "NtfySubscriberConn"
        private const val CONNECTION_LOOP_DELAY_MILLIS = 30_000L
        private const val RETRY_STEP_MILLIS = 5_000L
        private const val RETRY_MAX_MILLIS = 60_000L
        private const val RETRY_RESET_AFTER_MILLIS = 60_000L // Must be larger than CONNECTION_LOOP_DELAY_MILLIS
    }
}
