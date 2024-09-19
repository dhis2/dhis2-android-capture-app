import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketClient(private val context: Context, private val userId: String) : WebSocketListener() {

    private val TAG = "WebSocketClient"
    private val CHANNEL_ID = "CustomNotificationChannel"
    private lateinit var webSocket: WebSocket

    fun start() {
        val client = OkHttpClient()
        // Include userId as a query parameter
        val request = Request.Builder().url("ws://10.100.159.73:3000/?userId=$userId").build()
        webSocket = client.newWebSocket(request, this)
    }

    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        Log.d(TAG, "WebSocket Opened")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "Message received: $text")
        showNotification(text)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        Log.e(TAG, "WebSocket Error: ${t.message}")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "WebSocket Closed: $reason")
    }

    private fun showNotification(message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "WebSocket Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("WebSocket Notification")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        notificationManager.notify(1, notification)
    }
}
