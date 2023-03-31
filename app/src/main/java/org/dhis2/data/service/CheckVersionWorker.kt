package org.dhis2.data.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import javax.inject.Inject
import org.dhis2.usescases.main.MainPresenter

class CheckVersionWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @Inject
    internal lateinit var presenter: MainPresenter

    override fun doWork(): Result {
        presenter.checkVersionUpdate()
        return Result.success()
    }
}
