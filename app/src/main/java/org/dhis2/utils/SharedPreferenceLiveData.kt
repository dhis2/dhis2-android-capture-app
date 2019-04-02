package org.dhis2.utils

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

/**
 * Created by frodriguez on 3/28/2019.
 *
 */
abstract class SharedPreferenceLiveData<T>(val sharedPrefs: SharedPreferences,
                                           val key: String,
                                           val defValue: T) : LiveData<T>() {

    private val preferenceChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if(key == this.key)
            value = getValueFromPreferences(key, defValue)
    }

    abstract fun getValueFromPreferences(key: String, defValue: T): T

    override fun onActive() {
        super.onActive()
        value = getValueFromPreferences(key, defValue)
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangedListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangedListener)
        super.onInactive()
    }
}

class SharedPreferenceBooleanLiveData(sharedPrefs: SharedPreferences,
                                      key: String,
                                      defValue: Boolean) :
        SharedPreferenceLiveData<Boolean>(sharedPrefs, key, defValue) {

    override fun getValueFromPreferences(key: String, defValue: Boolean) = sharedPrefs.getBoolean(key, defValue)
}