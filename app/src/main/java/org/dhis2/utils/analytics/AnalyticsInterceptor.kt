/*
 * Copyright (c) 2004 - 2019, University of Oslo
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.utils.analytics

import io.reactivex.Single
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.dhis2.BuildConfig
import org.hisp.dhis.android.core.D2Manager

class AnalyticsInterceptor(private val analyticHelper: AnalyticsHelper) : Interceptor {

    private val appVersionName = BuildConfig.VERSION_NAME

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code >= 400 && isLogged()) {
            trackMatomoEvent(request, response)
        }
        return response
    }

    private fun trackMatomoEvent(request: Request, response: Response) {
        getDhis2Version()
            .subscribeOn(Schedulers.io())
            .subscribe(object : DisposableSingleObserver<String>() {
                override fun onSuccess(version: String) {
                    analyticHelper.trackMatomoEvent(
                        API_CALL,
                        "${request.method}_${request.url}",
                        "${response.code}_${appVersionName}_$version",
                    )
                    dispose()
                }

                override fun onError(e: Throwable) {
                    dispose()
                }
            })
    }

    private fun getDhis2Version(): Single<String> {
        return D2Manager.getD2().systemInfoModule().systemInfo().get().map { it.version() }
    }

    private fun isLogged(): Boolean {
        return D2Manager.getD2().userModule().blockingIsLogged()
    }
}
