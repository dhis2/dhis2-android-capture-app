package org.dhis2.data.qr

import android.graphics.Bitmap

import org.dhis2.usescases.qrCodes.QrViewModel

import io.reactivex.Observable

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

interface QRInterface {

    fun teiQRs(teiUid: String): Observable<List<QrViewModel>>

    fun eventWORegistrationQRs(eventUid: String): Observable<List<QrViewModel>>

    fun getUncodedData(teiUid: String): Observable<Bitmap>

    fun getNFCData(teiUid: String): Observable<ByteArray>

    fun setData(inputData: String): Observable<Boolean>

    fun decompress(dataToDecompress: ByteArray): String

    fun saveData(data: String): String
}