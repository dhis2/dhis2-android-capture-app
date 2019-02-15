package org.dhis2.data.qr;

import android.graphics.Bitmap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.usescases.qrCodes.QrViewModel;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import timber.log.Timber;

import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.LIMIT_1;
import static org.dhis2.data.database.SqlConstants.POINT;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.WHERE;
import static org.dhis2.data.qr.QRjson.ATTR_JSON;
import static org.dhis2.data.qr.QRjson.DATA_JSON;
import static org.dhis2.data.qr.QRjson.DATA_JSON_WO_REGISTRATION;
import static org.dhis2.data.qr.QRjson.ENROLLMENT_JSON;
import static org.dhis2.data.qr.QRjson.EVENTS_JSON;
import static org.dhis2.data.qr.QRjson.EVENT_JSON;
import static org.dhis2.data.qr.QRjson.TEI_JSON;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public class QRCodeGenerator implements QRInterface {

    private final BriteDatabase briteDatabase;
    private final Gson gson;

    private static final String TEI = SELECT + ALL + FROM + TrackedEntityInstanceModel.TABLE +
            WHERE + TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.UID +
            EQUAL + QUESTION_MARK + LIMIT_1;

    private static final String EVENT = SELECT + ALL + FROM + EventModel.TABLE +
            WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID + EQUAL + QUESTION_MARK + LIMIT_1;

    private static final String TEI_ATTR = SELECT + ALL + FROM + TrackedEntityAttributeValueModel.TABLE +
            WHERE + TrackedEntityAttributeValueModel.TABLE + POINT + TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE +
            EQUAL + QUESTION_MARK;

    private static final String TEI_DATA = SELECT + ALL + FROM + TrackedEntityDataValueModel.TABLE +
            WHERE + TrackedEntityDataValueModel.TABLE + POINT + TrackedEntityDataValueModel.Columns.EVENT +
            EQUAL + QUESTION_MARK;

    private static final String TEI_ENROLLMENTS = SELECT + ALL + FROM + EnrollmentModel.TABLE +
            WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE +
            EQUAL + QUESTION_MARK;

    private static final String TEI_EVENTS = SELECT + ALL + FROM + EventModel.TABLE +
            WHERE + EventModel.TABLE + POINT + EventModel.Columns.ENROLLMENT + EQUAL + QUESTION_MARK;

    QRCodeGenerator(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
        gson = new GsonBuilder().setDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION).create();
    }

    private List<QrViewModel> parseTrackedEntityAttributeValues(List<TrackedEntityAttributeValueModel> data) {
        List<QrViewModel> bitmaps = new ArrayList<>();
        ArrayList<TrackedEntityAttributeValueModel> arrayListAux = new ArrayList<>();
        // DIVIDE ATTR QR GENERATION -> 1 QR PER 2 ATTR
        int count = 0;
        for (int i = 0; i < data.size(); i++) {
            arrayListAux.add(data.get(i));
            if (count == 1) {
                count = 0;
                bitmaps.add(new QrViewModel(ATTR_JSON, gson.toJson(arrayListAux)));
                arrayListAux.clear();
            } else if (i == data.size() - 1) {
                bitmaps.add(new QrViewModel(ATTR_JSON, gson.toJson(arrayListAux)));
            } else {
                count++;
            }
        }
        return bitmaps;
    }

    private List<EnrollmentModel> parseEnrollments(List<EnrollmentModel> data, List<QrViewModel> bitmaps) {
        ArrayList<EnrollmentModel> arrayListAux = new ArrayList<>();
        // DIVIDE ENROLLMENT QR GENERATION -> 1 QR PER 2 ENROLLMENT
        int count = 0;
        for (int i = 0; i < data.size(); i++) {
            arrayListAux.add(data.get(i));
            if (count == 1) {
                count = 0;
                bitmaps.add(new QrViewModel(ENROLLMENT_JSON, gson.toJson(arrayListAux)));
                arrayListAux.clear();
            } else if (i == data.size() - 1) {
                bitmaps.add(new QrViewModel(ENROLLMENT_JSON, gson.toJson(arrayListAux)));
            } else {
                count++;
            }
        }
        return data;
    }

    private boolean parseTrackedEntityDataValues(List<TrackedEntityDataValueModel> dataValueList, List<QrViewModel> bitmaps) {
        ArrayList<TrackedEntityDataValueModel> arrayListAux = new ArrayList<>();
        // DIVIDE ATTR QR GENERATION -> 1 QR PER 2 ATTR
        int count = 0;
        for (int i = 0; i < dataValueList.size(); i++) {
            arrayListAux.add(dataValueList.get(i));
            if (count == 1) {
                count = 0;
                bitmaps.add(new QrViewModel(DATA_JSON, gson.toJson(arrayListAux)));
                arrayListAux.clear();
            } else if (i == dataValueList.size() - 1) {
                bitmaps.add(new QrViewModel(DATA_JSON, gson.toJson(arrayListAux)));
            } else {
                count++;
            }
        }
        return true;
    }

    @Override
    public Observable<List<QrViewModel>> teiQRs(String teiUid) {
        List<QrViewModel> bitmaps = new ArrayList<>();

        return
                briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, TEI, teiUid == null ? "" : teiUid)
                        .mapToOne(TrackedEntityInstanceModel::create)
                        .map(data -> bitmaps.add(new QrViewModel(TEI_JSON, gson.toJson(data))))


                        .flatMap(data -> briteDatabase.createQuery(TrackedEntityAttributeValueModel.TABLE, TEI_ATTR, teiUid == null ? "" : teiUid)
                                .mapToList(TrackedEntityAttributeValueModel::create))
                        .map(data -> bitmaps.addAll(parseTrackedEntityAttributeValues(data)))


                        /*  .flatMap(data -> briteDatabase.createQuery(RelationshipModel.TABLE, TEI_RELATIONSHIPS, teiUid, teiUid)
                                  .mapToList(RelationshipModel::create))*/
                        /* .flatMap(data->Observable.just(d2.relationshipModule().relationship.getRelationshipsByTEI(teiUid)))
                         .map(data -> bitmaps.add(new QrViewModel(RELATIONSHIP_JSON, gson.toJson(data))))*/


                        .flatMap(data -> briteDatabase.createQuery(EnrollmentModel.TABLE, TEI_ENROLLMENTS, teiUid == null ? "" : teiUid)
                                .mapToList(EnrollmentModel::create))
                        .map(data -> parseEnrollments(data, bitmaps))


                        .flatMap(data ->
                                Observable.fromIterable(data)
                                        .flatMap(enrollment -> briteDatabase.createQuery(EventModel.TABLE, TEI_EVENTS, enrollment.uid())
                                                .mapToList(EventModel::create)
                                        )
                        )
                        .flatMap(data ->
                                Observable.fromIterable(data)
                                        .flatMap(event -> {
                                                    bitmaps.add(new QrViewModel(EVENTS_JSON, gson.toJson(event)));
                                                    return briteDatabase.createQuery(TrackedEntityDataValueModel.TABLE, TEI_DATA, event.uid())
                                                            .mapToList(TrackedEntityDataValueModel::create)
                                                            .map(dataValueList -> parseTrackedEntityDataValues(dataValueList, bitmaps));
                                                }
                                        )
                        )
                        .map(data -> bitmaps);
    }


    @Override
    public Observable<List<QrViewModel>> eventWORegistrationQRs(String eventUid) {
        List<QrViewModel> bitmaps = new ArrayList<>();

        return
                briteDatabase.createQuery(EventModel.TABLE, EVENT, eventUid == null ? "" : eventUid)
                        .mapToOne(EventModel::create)
                        .map(data -> {
                            bitmaps.add(new QrViewModel(EVENT_JSON, gson.toJson(data)));
                            return data;
                        })
                        .flatMap(data -> briteDatabase.createQuery(TrackedEntityDataValueModel.TABLE, TEI_DATA, data.uid())
                                .mapToList(TrackedEntityDataValueModel::create))
                        .map(data -> {
                            ArrayList<TrackedEntityDataValueModel> arrayListAux = new ArrayList<>();
                            // DIVIDE ATTR QR GENERATION -> 1 QR PER 2 ATTR
                            int count = 0;
                            for (int i = 0; i < data.size(); i++) {
                                arrayListAux.add(data.get(i));
                                if (count == 1) {
                                    count = 0;
                                    bitmaps.add(new QrViewModel(DATA_JSON_WO_REGISTRATION, gson.toJson(arrayListAux)));
                                    arrayListAux.clear();
                                } else if (i == data.size() - 1) {
                                    bitmaps.add(new QrViewModel(DATA_JSON_WO_REGISTRATION, gson.toJson(arrayListAux)));
                                } else {
                                    count++;
                                }
                            }
                            return true;
                        })
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
