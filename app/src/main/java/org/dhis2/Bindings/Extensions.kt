package org.dhis2.Bindings

import androidx.lifecycle.MutableLiveData

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = this.apply { setValue(initialValue) }
