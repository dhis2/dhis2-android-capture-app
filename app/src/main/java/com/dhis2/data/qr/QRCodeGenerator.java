package com.dhis2.data.qr;

import android.graphics.Bitmap;

import com.dhis2.usescases.qrCodes.QrViewModel;
import com.dhis2.utils.DateUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import timber.log.Timber;

import static com.dhis2.data.qr.QRjson.ATTR_JSON;
import static com.dhis2.data.qr.QRjson.ENROLLMENT_JSON;
import static com.dhis2.data.qr.QRjson.EVENTS_JSON;
import static com.dhis2.data.qr.QRjson.TEI_JSON;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public class QRCodeGenerator implements QRInterface {


    private static final String TEI = "SELECT * FROM TrackedEntityInstance WHERE TrackedEntityInstance.uid = ?";
    private final BriteDatabase briteDatabase;
    private static final String TEI_ATTR = "SELECT * FROM " + TrackedEntityAttributeValueModel.TABLE +
            " WHERE " + TrackedEntityAttributeValueModel.TABLE + "." + TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE + " = ?";
    private static final String TEI_ENROLLMENTS = "SELECT * FROM Enrollment WHERE Enrollment." + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + " =?";
    private static final String TEI_EVENTS = "SELECT * FROM Event WHERE Event." + EventModel.Columns.ENROLLMENT + " =?";
    private final Gson gson;

    QRCodeGenerator(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
        gson = new GsonBuilder().setDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION).create();
    }

    @Override
    public Observable<List<QrViewModel>> teiQRs(String teiUid) {
        List<QrViewModel> bitmaps = new ArrayList<>();

        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, TEI, teiUid)
                .mapToOne(TrackedEntityInstanceModel::create)
                .map(data -> bitmaps.add(new QrViewModel(TEI_JSON, gson.toJson(data))))
                .flatMap(data -> briteDatabase.createQuery(TrackedEntityAttributeValueModel.TABLE, TEI_ATTR, teiUid)
                        .mapToList(TrackedEntityAttributeValueModel::create))
                .map(data -> bitmaps.add(new QrViewModel(ATTR_JSON, gson.toJson(data))))
                .flatMap(data -> briteDatabase.createQuery(EnrollmentModel.TABLE, TEI_ENROLLMENTS, teiUid)
                        .mapToList(EnrollmentModel::create))
                .map(data -> {
                    bitmaps.add(new QrViewModel(ENROLLMENT_JSON, gson.toJson(data)));
                    return data;
                })
                .flatMap(data ->
                        Observable.fromIterable(data)
                                .flatMap(enrollment -> briteDatabase.createQuery(EventModel.TABLE, TEI_EVENTS, enrollment.uid())
                                        .mapToList(EventModel::create)
                                        .map(eventList -> {
                                                    for (EventModel eventModel : eventList) {
                                                        bitmaps.add(new QrViewModel(EVENTS_JSON, gson.toJson(eventModel)));
                                                    }
                                                    return bitmaps;
                                                }
                                        )
                                )
                )
                .map(data -> bitmaps);
    }

    public static Bitmap transform(String type, String info) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Bitmap bitmap = null;
        Gson gson = new GsonBuilder().setDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION).create();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(gson.toJson(new QRjson(type, info)), BarcodeFormat.QR_CODE, 1000, 1000);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            Timber.e(e);
        }

        return bitmap;
    }
}
