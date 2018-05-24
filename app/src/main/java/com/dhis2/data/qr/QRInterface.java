package com.dhis2.data.qr;

import android.graphics.Bitmap;

import java.util.List;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public interface QRInterface {

    Observable<List<Bitmap>> teiQRs(String teiUid);

}
