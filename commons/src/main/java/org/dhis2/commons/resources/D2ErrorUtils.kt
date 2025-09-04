package org.dhis2.commons.resources

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.mobile.commons.resources.D2ErrorMessageProviderImpl

class D2ErrorUtils(
    private val context: Context,
    private val networkUtils: NetworkUtils,
) {
    private val d2ErrorMessageProvider = D2ErrorMessageProviderImpl()

    @Deprecated(
        message = "Use getErrorMessage instead",
        replaceWith = ReplaceWith("D2ErrorMessageProviderImpl"),
    )
    fun getErrorMessage(throwable: Throwable): String? =
        runBlocking {
            d2ErrorMessageProvider.getErrorMessage(throwable, networkUtils.isOnline())
        }
}
