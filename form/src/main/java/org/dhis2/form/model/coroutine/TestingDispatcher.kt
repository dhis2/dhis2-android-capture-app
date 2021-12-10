package org.dhis2.form.model.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import org.dhis2.form.model.DispatcherProvider

class TestingDispatcher : DispatcherProvider {
    override fun io(): CoroutineDispatcher {
        TODO("When 1.5 is supported migrate testing library and return TestingDispatcher")
    }

    override fun computation(): CoroutineDispatcher {
        TODO("When 1.5 is supported migrate testing library and return TestingDispatcher")
    }

    override fun ui(): CoroutineDispatcher {
        TODO("When 1.5 is supported migrate testing library and return TestingDispatcher")
    }
}
