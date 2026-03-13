package org.dhis2.commons

import timber.log.Timber

object CheckTime {
    var initTimer = 0L
    fun initTimer() {
        initTimer = System.currentTimeMillis()
    }

    fun elapsedTime(tag: String = "CHECK_TIMES", message: String) {
        val endTime = System.currentTimeMillis()
        val elapsedTime = endTime - initTimer
        Timber.tag(tag).d("$message at $elapsedTime")
    }
}