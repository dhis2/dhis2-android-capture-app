package org.dhis2.common.coroutine

import androidx.test.espresso.idling.concurrent.IdlingThreadPoolExecutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.dhis2.form.model.DispatcherProvider
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

val IDLING_THREAD_POOL = "IdlingThreadPoolDispatcher"
val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()

class TestingDispatcher : DispatcherProvider {
    override fun io(): CoroutineDispatcher {
        val idlingThreadPool = IdlingThreadPoolExecutor(
            IDLING_THREAD_POOL,
            NUMBER_OF_CORES * 2,
            NUMBER_OF_CORES * 2,
            60L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>()
        ) { Thread(it) }

        return idlingThreadPool.asCoroutineDispatcher()
    }

    override fun computation(): CoroutineDispatcher {
        return Dispatchers.Default
    }

    override fun ui(): CoroutineDispatcher {
        return Dispatchers.Main
    }
}
