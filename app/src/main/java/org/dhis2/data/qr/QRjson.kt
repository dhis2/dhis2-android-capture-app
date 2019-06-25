package org.dhis2.data.qr

import android.util.Base64

import java.io.UnsupportedEncodingException

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

class QRjson internal constructor(val type: String, private val data: String) {

    @Throws(UnsupportedEncodingException::class)
    fun getData(): String {
        val info: String
        val decodedBytes = Base64.decode(data, Base64.DEFAULT)
        info = String(decodedBytes, Charsets.UTF_8)

        return info
    }

    companion object {
        const val EVENT_JSON = "EVENT"
        const val DATA_JSON_WO_REGISTRATION = "DATA_WO_REGISTRATION"
        const val DATA_JSON = "DATA"

        const val TEI_JSON = "TEI"
        const val ENROLLMENT_JSON = "ENROLLMENT"
        const val EVENTS_JSON = "EVENTS"
        const val ATTR_JSON = "ATTR"
        const val RELATIONSHIP_JSON = "RELATIONSHIP"
    }
}
