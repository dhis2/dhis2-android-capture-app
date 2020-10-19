package org.dhis2.common.rules

import android.util.Log
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RetryRule(val retryCount: Int = 3) : TestRule {

    private val TAG = RetryRule::class.java.simpleName

    override fun apply(base: Statement, description: Description): Statement {
        return statement(base, description)
    }

    private fun statement(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                var caughtThrowable: Throwable? = null

                for (i in 0 until retryCount) {
                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        caughtThrowable = t
                        Log.e(TAG, description.displayName + ": run " + (i + 1) + " failed")
                    }
                }

                Log.e(TAG, description.displayName + ": giving up after " + retryCount + " failures")
                throw caughtThrowable!!
            }
        }
    }
}