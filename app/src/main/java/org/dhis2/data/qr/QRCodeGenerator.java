package org.dhis2.data.qr;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Base64;

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
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.period.FeatureType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static java.util.zip.Deflater.BEST_COMPRESSION;
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
    private final D2 d2;
    private final Gson gson;
    private static final String TEI_FLAG = "$T";
    private static final String ENROLLMENT_FLAG = "$R";
    private static final String EVENT_FLAG = "$E";
    private static final String ATTR_FLAG = "$A";
    private static final String DE_FLAG = "$D";

    private static final Pattern TEI_PATTERN = Pattern.compile("\\$T(.+)");
    private static final Pattern ENROLLMENT_PATTERN = Pattern.compile("\\$R(.+)");
    private static final Pattern EVENT_PATTERN = Pattern.compile("\\$E(.+)");
    private static final Pattern ATTR_PATTERN = Pattern.compile("\\$A(.+)");
    private static final Pattern DE_PATTERN = Pattern.compile("\\$D(.+)");


    private static String data;

    private static final String TEI = "SELECT * FROM " + TrackedEntityInstanceModel.TABLE + " WHERE " + TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.UID + " = ? LIMIT 1";

    private static final String EVENT = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.TABLE + "." + EventModel.Columns.UID + " = ? LIMIT 1";

    private static final String TEI_ATTR = "SELECT * FROM " + TrackedEntityAttributeValueModel.TABLE + " WHERE " + TrackedEntityAttributeValueModel.TABLE + "." + TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE + " = ?";

    private static final String TEI_DATA = "SELECT * FROM " + TrackedEntityDataValueModel.TABLE + " WHERE " + TrackedEntityDataValueModel.TABLE + "." + TrackedEntityDataValueModel.Columns.EVENT + " = ?";

    private static final String TEI_ENROLLMENTS = "SELECT * FROM " + EnrollmentModel.TABLE + " WHERE " + EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + " = ?";

    private static final String TEI_EVENTS = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.TABLE + "." + EventModel.Columns.ENROLLMENT + " =?";

   /* private static final String TEI_RELATIONSHIPS = "SELECT * FROM " + RelationshipModel.TABLE + " WHERE " + RelationshipModel.TABLE + "." + RelationshipModel.Columns.TRACKED_ENTITY_INSTANCE_A + " = ? OR " +
            RelationshipModel.TABLE + "." + RelationshipModel.Columns.TRACKED_ENTITY_INSTANCE_B + " = ?";*/


    public QRCodeGenerator(BriteDatabase briteDatabase, D2 d2) {
        this.briteDatabase = briteDatabase;
        gson = new GsonBuilder().setDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION).create();
        this.d2 = d2;
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
                        .map(data -> {
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
                            return true;
                        })


                        .flatMap(data -> briteDatabase.createQuery(EnrollmentModel.TABLE, TEI_ENROLLMENTS, teiUid == null ? "" : teiUid)
                                .mapToList(EnrollmentModel::create))
                        .map(data -> {
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
                        })


                        .flatMap(data ->
                                Observable.fromIterable(data)
                                        .flatMap(enrollment -> briteDatabase.createQuery(EventModel.TABLE, TEI_EVENTS, enrollment.uid() == null ? "" : enrollment.uid())
                                                .mapToList(EventModel::create)
                                        )
                        )
                        .flatMap(data ->
                                Observable.fromIterable(data)
                                        .flatMap(event -> {
                                                    bitmaps.add(new QrViewModel(EVENTS_JSON, gson.toJson(event)));
                                                    return briteDatabase.createQuery(TrackedEntityDataValueModel.TABLE, TEI_DATA, event.uid() == null ? "" : event.uid())
                                                            .mapToList(TrackedEntityDataValueModel::create)
                                                            .map(dataValueList -> {
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
                                                            });
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
                        .flatMap(data -> briteDatabase.createQuery(TrackedEntityDataValueModel.TABLE, TEI_DATA, data.uid() == null ? "" : data.uid())
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
        byte[] data;
        String encoded;
        try {
            data = info.getBytes("UTF-8");
            encoded = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            Timber.e(e);
            encoded = e.getLocalizedMessage();
        }

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Bitmap bitmap = null;
        Gson gson = new GsonBuilder().setDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION).create();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(gson.toJson(new QRjson(type, encoded)), BarcodeFormat.QR_CODE, 1000, 1000);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            Timber.e(e);
        }

        return bitmap;
    }

    public static Bitmap transform(byte[] inputData) {
        String encoded;
        encoded = Base64.encodeToString(inputData, Base64.DEFAULT);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Bitmap bitmap = null;
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(encoded, BarcodeFormat.QR_CODE, 1000, 1000);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            Timber.e(e);
        }

        return bitmap;
    }

    private String decodeData(String data) {
        byte[] decodedBytes = Base64.decode(data, Base64.DEFAULT);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    @Override
    public Observable<Bitmap> getUncodedData(String teiUid) {
        return Observable.fromCallable(() -> getData(teiUid))
                .map(this::compress)
                .map(QRCodeGenerator::transform);
    }

    @Override
    public Observable<Boolean> setData(String inputData) {
        return Observable.fromCallable(() -> decompress(decodeData(inputData).getBytes()))
                .map(data -> getTEIInfo(data));
    }

    private Boolean getTEIInfo(String formattedData) {
        String initialString = TEI_PATTERN.matcher(formattedData).group(1);
        String tei_substring = initialString.substring(0,initialString.indexOf(ENROLLMENT_FLAG));

        String[] tei_substring_split = tei_substring.split("|");

        TrackedEntityInstance tei = TrackedEntityInstance.builder()
                .uid(tei_substring_split[0])
                .created(/*DateUtils.databaseDateFormat().parse(teiSubstring[1])*/new Date())
                .organisationUnit(tei_substring_split[2])
                .trackedEntityType(tei_substring_split[3])
                .featureType(!isEmpty(tei_substring_split[4]) ? FeatureType.valueOf(tei_substring_split[4]) : null)
                .coordinates(!isEmpty(tei_substring_split[5]) ? tei_substring_split[5] : null)
                .state(State.valueOf(tei_substring_split[6]))
                .build();

        return true;
    }

    public byte[] compress(String dataToCompress) {
        byte[] input = dataToCompress.getBytes(StandardCharsets.UTF_8);
        Deflater compresser = new Deflater(BEST_COMPRESSION);
        compresser.setInput(input);
        compresser.finish();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        while (!compresser.finished()) {
            int byteCount = compresser.deflate(buf);
            baos.write(buf, 0, byteCount);
        }
        compresser.end();
        return baos.toByteArray();
    }

    public String decompress(byte[] dataToDecompress) {
        try {
            Inflater decompresser = new Inflater();
            decompresser.setInput(dataToDecompress);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataToDecompress.length);
            byte[] buffer = new byte[1024];
            while (!decompresser.finished()) {
                int count = decompresser.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            byte[] output = outputStream.toByteArray();
            decompresser.end();
            return new String(output, 0, output.length, StandardCharsets.UTF_8);
        } catch (DataFormatException e) {
            Timber.e(e);
            return "";
        } catch (IOException e) {
            Timber.e(e);
            return "";
        }
    }

    private String getData(String teiUid) {
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append(TEI_FLAG);
        TrackedEntityInstance tei = d2.trackedEntityModule().trackedEntityInstances.uid(teiUid).get();
        dataBuilder.append(setTeiData(tei));
        List<Enrollment> enrollments = d2.enrollmentModule().enrollments.byTrackedEntityInstance().eq(teiUid).get();
        for (Enrollment enrollment : enrollments) {
            dataBuilder.append(ENROLLMENT_FLAG);
            dataBuilder.append(setEnrollmentData(enrollment));
            List<TrackedEntityAttributeValue> teAttrValues = d2.trackedEntityModule().trackedEntityAttributeValues.byTrackedEntityInstance().eq(teiUid).get();
            for (TrackedEntityAttributeValue attrValue : teAttrValues) {
                dataBuilder.append(ATTR_FLAG);
                dataBuilder.append(setAttrData(attrValue));
            }
            List<Event> events = d2.eventModule().events.byEnrollmentUid().eq(enrollment.uid()).get();
            for (Event event : events) {
                dataBuilder.append(EVENT_FLAG);
                dataBuilder.append(setEventData(event));
                List<TrackedEntityDataValue> teDataValue = d2.trackedEntityModule().trackedEntityDataValues.byEvent().eq(event.uid()).get();
                for (TrackedEntityDataValue dataValue : teDataValue) {
                    dataBuilder.append(DE_FLAG);
                    dataBuilder.append(setTEDataValue(dataValue));
                }
            }
        }

        return dataBuilder.toString();
    }

    private String setAttrData(TrackedEntityAttributeValue attrValue) {
        List<String> data = new ArrayList<>();
        data.add(attrValue.trackedEntityAttribute());
        data.add(attrValue.value());
        return TextUtils.join("|", data);
    }

    private String setTEDataValue(TrackedEntityDataValue dataValue) {
        List<String> data = new ArrayList<>();
        data.add(dataValue.dataElement());
        data.add(dataValue.value());
        return TextUtils.join("|", data);
    }

    private String setEventData(Event event) {
        List<String> data = new ArrayList<>();
        data.add(event.uid());
        data.add(DateUtils.databaseDateFormat().format(event.created()));
        data.add(event.status().name());
        data.add(event.coordinate() != null ? String.valueOf(event.coordinate().latitude()) : "");
        data.add(event.coordinate() != null ? String.valueOf(event.coordinate().longitude()) : "");
        data.add(event.program());
        data.add(event.program()); //TEI OR ENROLLMENT?
        data.add(event.programStage());
        data.add(event.eventDate() != null ? DateUtils.databaseDateFormat().format(event.eventDate()) : "");
        data.add(event.completedDate() != null ? DateUtils.databaseDateFormat().format(event.completedDate()) : "");
        data.add(event.dueDate() != null ? DateUtils.databaseDateFormat().format(event.created()) : "");
        data.add(event.state() != null ? event.state().name() : "");
        return TextUtils.join("|", data);
    }

    private String setTeiData(TrackedEntityInstance tei) {
        List<String> data = new ArrayList<>();
        data.add(tei.uid());
        data.add(DateUtils.databaseDateFormat().format(tei.created()));
        data.add(tei.organisationUnit());
        data.add(tei.trackedEntityType());
        data.add(tei.featureType() != null ? tei.featureType().name() : "");
        data.add(tei.coordinates());
        data.add(tei.state() != null ? tei.state().name() : "");
        return TextUtils.join("|", data);
    }

    private String setEnrollmentData(Enrollment enrollment) {
        List<String> data = new ArrayList<>();
        data.add(enrollment.uid());
        data.add(DateUtils.databaseDateFormat().format(enrollment.created()));
        data.add(enrollment.organisationUnit());
        data.add(enrollment.program());
        data.add(enrollment.enrollmentDate() != null ? DateUtils.databaseDateFormat().format(enrollment.enrollmentDate()) : "");
        data.add(enrollment.incidentDate() != null ? DateUtils.databaseDateFormat().format(enrollment.incidentDate()) : "");
        data.add(enrollment.followUp() ? "t" : "f");
        data.add(enrollment.status().name());
        data.add(enrollment.coordinate() != null ? String.valueOf(enrollment.coordinate().latitude()) : "");
        data.add(enrollment.coordinate() != null ? String.valueOf(enrollment.coordinate().longitude()) : "");
        data.add(enrollment.state().name());
        return TextUtils.join("|", data);
    }
}
