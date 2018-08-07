package com.dhis2.data.qr;

import com.dhis2.usescases.qrCodes.QrViewModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public interface QRInterface {

    Observable<List<QrViewModel>> teiQRs(String teiUid);

    Observable<List<QrViewModel>> eventWORegistrationQRs(String eventUid);

}