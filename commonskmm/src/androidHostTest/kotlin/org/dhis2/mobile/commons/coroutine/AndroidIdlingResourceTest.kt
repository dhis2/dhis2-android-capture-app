package org.dhis2.mobile.commons.coroutine

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AndroidIdlingResourceTest {
    private val pool = Executors.newFixedThreadPool(THREADS)

    @AfterTest
    fun tearDown() {
        pool.shutdownNow()
    }

    @Test
    fun `concurrent increment and decrement pairs leave the resource idle`() {
        val start = CountDownLatch(1)
        val tasks =
            List(THREADS) {
                pool.submit {
                    start.await()
                    repeat(ITERATIONS) {
                        AndroidIdlingResource.increment()
                        AndroidIdlingResource.decrement()
                    }
                }
            }

        start.countDown()
        // get() rethrows a "Counter has been corrupted!" raised by a racing decrement.
        tasks.forEach { it.get(30, TimeUnit.SECONDS) }

        assertTrue(AndroidIdlingResource.getIdlingResource().isIdleNow)
    }

    private companion object {
        const val THREADS = 8
        const val ITERATIONS = 20_000
    }
}
