package org.dhis2.utils.reporting

import io.sentry.Breadcrumb
import io.sentry.HubAdapter
import io.sentry.IHub
import io.sentry.Sentry
import io.sentry.SpanStatus
import io.sentry.protocol.User
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class SentryOkHttpInterceptor(
    private val hub: IHub = HubAdapter.getInstance()
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val url = request.url().toString()
        val endpoint = "TEST - " + url.split("?").first()
        val method = request.method()

        val transaction = Sentry.startTransaction(endpoint, "http.client")
        Sentry.configureScope { scope -> scope.setTransaction(transaction) }

        // read transaction from the bound scope
        val span = hub.span?.startChild("http.client", "$method $url")

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
            code?.let{
                breadcrumb.setData("status_code", it)
            }
            request.body()?.contentLength().ifHasValidLength {
                breadcrumb.setData("requestBodySize", it)
            }
            response?.body()?.contentLength().ifHasValidLength {
                breadcrumb.setData("responseBodySize", it)
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
}
