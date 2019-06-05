package org.dhis2.data.qr;

import android.graphics.Bitmap;

import org.dhis2.usescases.qrCodes.QrViewModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public interface QRInterface {

    Observable<List<QrViewModel>> teiQRs(String teiUid);

    Observable<List<QrViewModel>> eventWORegistrationQRs(String eventUid);

    Observable<Bitmap> getUncodedData(String teiUid);

    Observable<Boolean> setData(String inputData);
}