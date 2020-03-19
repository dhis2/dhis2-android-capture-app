package org.dhis2.common.keystore

import org.hisp.dhis.android.core.arch.storage.internal.AndroidSecureStore

class KeyStoreRobot (private val keystore: AndroidSecureStore) {

    fun setData(key:String, value:String){
        keystore.setData(key, value)
    }

    fun getData(key:String){
        keystore.getData(key)
    }

    fun removeData(key:String){
        keystore.removeData(key)
    }

    companion object {
        const val KEYSTORE_USERNAME = "username"
        const val KEYSTORE_PASSWORD = "password"
    }
}