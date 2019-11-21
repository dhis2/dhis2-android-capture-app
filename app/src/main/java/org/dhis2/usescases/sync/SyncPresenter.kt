/*
* Copyright (c) 2004-2019, University of Oslo
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright notice, this
* list of conditions and the following disclaimer.
*
* Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.
* Neither the name of the HISP project nor the names of its contributors may
* be used to endorse or promote products derived from this software without
* specific prior written permission.
*
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

package org.dhis2.usescases.sync

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.data.tuples.Pair
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.SystemSetting
import timber.log.Timber

class SyncPresenter constructor(
    private val view: SyncView,
    private val d2: D2,
    private val schedulerProvider: SchedulerProvider,
    private val workManagerController: WorkManagerController
) {

    val disposable = CompositeDisposable()

    fun sync() {
        workManagerController
            .syncDataForWorkers(Constants.META_NOW, Constants.DATA_NOW, Constants.INITIAL_SYNC)
    }

    fun getTheme() {
        disposable.add(
            d2.systemSettingModule().systemSetting().get()
                .map { systemSettings ->
                    var style = ""
                    var flag = ""
                    for (setting in systemSettings) {
                        if (setting.key() == SystemSetting.SystemSettingKey.STYLE) {
                            style = setting.value() ?: ""
                        } else {
                            flag = setting.value() ?: ""
                        }
                    }
                    Pair.create(flag, style)
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { flagTheme ->
                        view.saveFlag(flagTheme.val0())
                        view.saveTheme(flagTheme.val1())
                    },
                    Timber::e
                )
        )
    }

    fun syncReservedValues() {
        val workerItem = WorkerItem(Constants.RESERVED, WorkerType.RESERVED)
        workManagerController.cancelAllWorkByTag(workerItem.workerName)
        workManagerController.syncDataForWorker(workerItem)
    }

    fun onDettach() = disposable.clear()

    fun displayMessage(message: String) = view.displayMessage(message)
}
