package org.dhis2.utils.reporting

import android.net.Uri
import io.sentry.Breadcrumb
import io.sentry.HubAdapter
import io.sentry.IHub
import io.sentry.Sentry
import io.sentry.SpanStatus
import io.sentry.protocol.User
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response
import org.dhis2.commons.Constants.SERVER
import org.dhis2.commons.Constants.USER
import org.dhis2.commons.prefs.PreferenceProvider

/**
 * Usage:
 * In ServerModule.kt add
 * interceptors.add(
 *        SentryOkHttpInterceptor(
 *           context.app().appComponent().preferenceProvider()
 *        )
 * )
 */
class SentryOkHttpInterceptor(
    val preferenceProvider: PreferenceProvider,
    private val hub: IHub = HubAdapter.getInstance()
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val url = request.url().toString()
        val path = Uri.parse(url).path?.split("api")?.get(1) ?: url
        val method = request.method()

        val transaction = Sentry.startTransaction(path, OPERATION)
        Sentry.configureScope { scope ->
            scope.transaction = transaction
            scope.user = User().apply {
                username = preferenceProvider.getString(USER)
            }
            scope.setTag(SERVER_NAME, preferenceProvider.getString(SERVER) ?: "")
        }

        val span = hub.span?.startChild(OPERATION, "$method $url")

        var response: Response? = null

        var code: Int? = null
        try {
            span?.toSentryTrace()?.let {
                request = request.newBuilder().addHeader(it.name, it.value).build()
            }
            response = chain.proceed(request)
            code = response.code()
            return response
        } catch (e: IOException) {
            span?.throwable = e
            throw e
        } finally {
            span?.finish(code?.let { SpanStatus.fromHttpStatusCode(it, SpanStatus.INTERNAL_ERROR) })

            val breadcrumb = Breadcrumb.http(request.url().toString(), request.method())
            code?.let {
                breadcrumb.setData(STATUS_CODE, it)
            }
            request.body()?.contentLength().ifHasValidLength {
                breadcrumb.setData(REQUEST_SIZE, it)
            }
            response?.body()?.contentLength().ifHasValidLength {
                breadcrumb.setData(RESPONSE_SIZE, it)
            }
            hub.addBreadcrumb(breadcrumb)

            transaction.finish()
        }
    }

    private fun Long?.ifHasValidLength(fn: (Long) -> Unit) {
        if (this != null && this != -1L) {
            fn.invoke(this)
        }
    }

    companion object {
        const val SERVER_NAME = "server_name"
        const val OPERATION = "http.client"
        const val STATUS_CODE = "status_code"
        const val REQUEST_SIZE = "requestBodySize"
        const val RESPONSE_SIZE = "responseBodySize"
    }
}
