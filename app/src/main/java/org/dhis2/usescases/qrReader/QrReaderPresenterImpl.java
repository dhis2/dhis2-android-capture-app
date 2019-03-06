package org.dhis2.usescases.qrReader;

import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.COMMA;
import static org.dhis2.data.database.SqlConstants.DATA_ELEMENT_FORM_NAME;
import static org.dhis2.data.database.SqlConstants.DATA_ELEMENT_TABLE;
import static org.dhis2.data.database.SqlConstants.DATA_ELEMENT_UID;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_DISPLAY_NAME;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_TABLE;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_UID;
import static org.dhis2.data.database.SqlConstants.WHERE;
import static org.dhis2.utils.DateUtils.DATABASE_FORMAT_EXPRESSION;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

class QrReaderPresenterImpl implements QrReaderContracts.Presenter {

    private final BriteDatabase briteDatabase;
    private final D2 d2;
    private QrReaderContracts.View view;
    private CompositeDisposable compositeDisposable;

    private JSONObject eventWORegistrationJson;
    private String eventUid;
    private ArrayList<JSONObject> dataJson = new ArrayList<>();
    private ArrayList<JSONObject> teiDataJson = new ArrayList<>();

    private JSONObject teiJson;
    private List<JSONArray> attrJson = new ArrayList<>();
    private List<JSONArray> enrollmentJson = new ArrayList<>();
    private List<JSONArray> relationshipsJson = new ArrayList<>();
    private ArrayList<JSONObject> eventsJson = new ArrayList<>();
    private String teiUid;
    private static final String TIMBER_MESSAGE = "insert event %l";

    QrReaderPresenterImpl(BriteDatabase briteDatabase, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.d2 = d2;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void handleEventWORegistrationInfo(JSONObject jsonObject) {
        this.eventWORegistrationJson = jsonObject;
        eventUid = null;
        try {
            eventUid = jsonObject.getString(EventModel.Columns.UID);
        } catch (JSONException e) {
            Timber.e(e);
        }

        view.renderEventWORegistrationInfo(eventUid);
    }

    @Override
    public void handleDataWORegistrationInfo(JSONArray jsonArray) {
        if (eventUid != null) {
            try {
                // LOOK FOR TRACKED ENTITY ATTRIBUTES ON LOCAL DATABASE
                view.renderEventDataInfo(getAttributes(jsonArray));
            } catch (JSONException | ParseException e) {
                Timber.e(e);
            }
        }
    }

    private ArrayList<Trio<TrackedEntityDataValueModel, String, Boolean>> getAttributes(JSONArray jsonArray) throws JSONException, ParseException {
        ArrayList<Trio<TrackedEntityDataValueModel, String, Boolean>> attributes = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject attrValue = jsonArray.getJSONObject(i);

            TrackedEntityDataValueModel.Builder trackedEntityDataValueModelBuilder = parseTEDataValueJson(attrValue);

            if (attrValue.has(TrackedEntityDataValueModel.Columns.DATA_ELEMENT) &&
                    attrValue.getString(TrackedEntityDataValueModel.Columns.DATA_ELEMENT) != null) {
                // LOOK FOR dataElement ON LOCAL DATABASE.
                Cursor cursor = briteDatabase.query(SELECT + ALL + FROM + DATA_ELEMENT_TABLE +
                        WHERE + DATA_ELEMENT_UID + EQUAL + QUESTION_MARK, attrValue.getString(
                        TrackedEntityDataValueModel.Columns.DATA_ELEMENT));
                // IF FOUND, OPEN DASHBOARD
                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                    this.dataJson.add(attrValue);
                    attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), cursor.getString(cursor.getColumnIndex("formName")), true));
                } else {
                    attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), null, false));
                }
            } else {
                attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), null, false));
            }
        }
        return attributes;
    }

    private TrackedEntityDataValueModel.Builder parseTEDataValueJson(JSONObject attrValue) throws JSONException, ParseException {
        TrackedEntityDataValueModel.Builder trackedEntityDataValueModelBuilder = getTEDataValueBuilder(attrValue);
        trackedEntityDataValueModelBuilder.event(eventUid);

        return trackedEntityDataValueModelBuilder;
    }

    private TrackedEntityDataValueModel.Builder parseTEDataInfoJson(JSONObject attrValue) throws JSONException, ParseException {
        TrackedEntityDataValueModel.Builder trackedEntityDataValueModelBuilder = getTEDataValueBuilder(attrValue);
        if (attrValue.has(TrackedEntityDataValueModel.Columns.EVENT)) {
            trackedEntityDataValueModelBuilder.event(attrValue.getString(TrackedEntityDataValueModel.Columns.EVENT));
        }
        return trackedEntityDataValueModelBuilder;
    }

    private TrackedEntityDataValueModel.Builder getTEDataValueBuilder(JSONObject attrValue) throws JSONException, ParseException {
        TrackedEntityDataValueModel.Builder trackedEntityDataValueModelBuilder = TrackedEntityDataValueModel.builder();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATABASE_FORMAT_EXPRESSION, Locale.getDefault());
        if (attrValue.has(TrackedEntityDataValueModel.Columns.DATA_ELEMENT)) {
            trackedEntityDataValueModelBuilder.dataElement(attrValue.getString(TrackedEntityDataValueModel.Columns.DATA_ELEMENT));
        }
        if (attrValue.has(TrackedEntityDataValueModel.Columns.STORED_BY)) {
            trackedEntityDataValueModelBuilder.storedBy(attrValue.getString(TrackedEntityDataValueModel.Columns.STORED_BY));
        }
        if (attrValue.has(TrackedEntityDataValueModel.Columns.VALUE)) {
            trackedEntityDataValueModelBuilder.value(attrValue.getString(TrackedEntityDataValueModel.Columns.VALUE));
        }
        if (attrValue.has(TrackedEntityDataValueModel.Columns.PROVIDED_ELSEWHERE)) {
            trackedEntityDataValueModelBuilder.providedElsewhere(Boolean.parseBoolean(attrValue.getString(TrackedEntityDataValueModel.Columns.PROVIDED_ELSEWHERE)));
        }
        if (attrValue.has(TrackedEntityDataValueModel.Columns.CREATED)) {
            trackedEntityDataValueModelBuilder.created(simpleDateFormat.parse(attrValue.getString(TrackedEntityDataValueModel.Columns.CREATED)));
        }
        if (attrValue.has(TrackedEntityDataValueModel.Columns.LAST_UPDATED)) {
            trackedEntityDataValueModelBuilder.lastUpdated(simpleDateFormat.parse(attrValue.getString(TrackedEntityDataValueModel.Columns.LAST_UPDATED)));
        }

        return trackedEntityDataValueModelBuilder;
    }

    @Override
    public void handleDataInfo(JSONArray jsonArray) {
        ArrayList<Trio<TrackedEntityDataValueModel, String, Boolean>> attributes = new ArrayList<>();
        try {
            // LOOK FOR TRACKED ENTITY ATTRIBUTES ON LOCAL DATABASE
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject attrValue = jsonArray.getJSONObject(i);
                TrackedEntityDataValueModel.Builder trackedEntityDataValueModelBuilder = parseTEDataInfoJson(attrValue);

                if (attrValue.has(TrackedEntityDataValueModel.Columns.DATA_ELEMENT) &&
                        attrValue.getString(TrackedEntityDataValueModel.Columns.DATA_ELEMENT) != null) {
                    // LOOK FOR dataElement ON LOCAL DATABASE.
                    Cursor cursor = briteDatabase.query(SELECT + ALL + FROM + DATA_ELEMENT_TABLE +
                                    WHERE + DATA_ELEMENT_UID + EQUAL + QUESTION_MARK,
                            attrValue.getString(TrackedEntityDataValueModel.Columns.DATA_ELEMENT));
                    // IF FOUND, OPEN DASHBOARD
                    if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                        this.teiDataJson.add(attrValue);
                        attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(),
                                cursor.getString(cursor.getColumnIndex(DATA_ELEMENT_FORM_NAME)), true));
                    } else {
                        attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), null, false));
                    }
                } else {
                    attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), null, false));
                }
            }
        } catch (JSONException | ParseException e) {
            Timber.e(e);
        }

        view.renderTeiEventDataInfo(attributes);
    }

    @Override
    public void handleTeiInfo(JSONObject jsonObject) {
        this.teiJson = jsonObject;
        teiUid = null;
        try {
            teiUid = jsonObject.getString(TrackedEntityInstanceModel.Columns.UID);
        } catch (JSONException e) {
            Timber.e(e);
        }

        // IF TEI READ
        if (teiUid != null) {
            // LOOK FOR TEI ON LOCAL DATABASE.
            Cursor cursor = briteDatabase.query(SELECT + ALL + FROM + TrackedEntityInstanceModel.TABLE +
                    WHERE + TrackedEntityInstanceModel.Columns.UID + EQUAL + QUESTION_MARK, teiUid);
            // IF FOUND, OPEN DASHBOARD
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                view.goToDashBoard(teiUid);
                cursor.close();
            }
            // IF NOT FOUND, TRY TO DOWNLOAD ONLINE, OR PROMPT USER TO SCAN MORE QR CODES
            else {
                view.downloadTei(teiUid);
            }
        }
        // IF NO TEI PRESENT ON THE QR, SHOW ERROR
        else
            view.renderTeiInfo(null);
    }

    @Override
    public void handleAttrInfo(JSONArray jsonArray) {
        this.attrJson.add(jsonArray);
        ArrayList<Trio<String, String, Boolean>> attributes = new ArrayList<>();
        try {
            // LOOK FOR TRACKED ENTITY ATTRIBUTES ON LOCAL DATABASE
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject attrValue = jsonArray.getJSONObject(i);
                if (attrValue.has(TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE) &&
                        attrValue.getString(TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE) != null) {
                    Cursor cursor = briteDatabase.query(SELECT +
                                    TE_ATTR_UID + COMMA +
                                    TE_ATTR_DISPLAY_NAME +
                                    FROM + TE_ATTR_TABLE +
                                    WHERE + TE_ATTR_UID +
                                    EQUAL + QUESTION_MARK,
                            attrValue.getString(TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE));
                    // TRACKED ENTITY ATTRIBUTE FOUND, TRACKED ENTITY ATTRIBUTE VALUE CAN BE SAVED.
                    if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                        attributes.add(Trio.create(cursor.getString(1),
                                attrValue.getString(TrackedEntityAttributeValueModel.Columns.VALUE),
                                true));
                        cursor.close();
                    }
                    // TRACKED ENTITY ATTRIBUTE NOT FOUND, TRACKED ENTITY ATTRIBUTE VALUE CANNOT BE SAVED.
                    else {
                        attributes.add(Trio.create(attrValue.getString(TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE),
                                "", false));
                    }
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        view.renderAttrInfo(attributes);
    }

    @Override
    public void handleEnrollmentInfo(JSONArray jsonArray) {
        this.enrollmentJson.add(jsonArray);
        ArrayList<Pair<String, Boolean>> enrollments = new ArrayList<>();
        try {
            // LOOK FOR PROGRAM ON LOCAL DATABASE
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject attrValue = jsonArray.getJSONObject(i);
                if (attrValue.has(EnrollmentModel.Columns.PROGRAM) && attrValue.getString(EnrollmentModel.Columns.PROGRAM) != null) {
                    Cursor cursor = briteDatabase.query(SELECT +
                                    ProgramModel.Columns.UID + COMMA +
                                    ProgramModel.Columns.DISPLAY_NAME +
                                    FROM + ProgramModel.TABLE +
                                    WHERE + ProgramModel.Columns.UID +
                                    EQUAL + QUESTION_MARK,
                            attrValue.getString(EnrollmentModel.Columns.PROGRAM));
                    // PROGRAM FOUND, ENROLLMENT CAN BE SAVED
                    if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                        enrollments.add(Pair.create(cursor.getString(1), true));
                        cursor.close();
                    }
                    // PROGRAM NOT FOUND, ENROLLMENT CANNOT BE SAVED
                    else {
                        enrollments.add(Pair.create(attrValue.getString(EnrollmentModel.Columns.UID), false));
                    }
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        view.renderEnrollmentInfo(enrollments);
    }

    private ArrayList<Pair<String, Boolean>> getEventsFromQR(JSONObject jsonObject) throws JSONException {
        ArrayList<Pair<String, Boolean>> events = new ArrayList<>();
        boolean isEnrollmentReadFromQr = false;
        for (int i = 0; i < enrollmentJson.size(); i++) {
            JSONArray enrollmentArray = enrollmentJson.get(i);
            for (int j = 0; j < enrollmentArray.length(); j++) {
                JSONObject enrollment = enrollmentArray.getJSONObject(j);
                if (jsonObject.getString(EventModel.Columns.ENROLLMENT).equals(enrollment.getString(EnrollmentModel.Columns.UID))) {
                    isEnrollmentReadFromQr = true;
                    break;
                }
            }
        }
        if (isEnrollmentReadFromQr) {
            events.add(Pair.create(jsonObject.getString(EnrollmentModel.Columns.UID), true));
        } else {
            events.add(Pair.create(jsonObject.getString(EnrollmentModel.Columns.UID), false));
        }
        return events;
    }

    @Override
    public void handleEventInfo(JSONObject jsonObject) {
        this.eventsJson.add(jsonObject);
        ArrayList<Pair<String, Boolean>> events = new ArrayList<>();
        try {
            // LOOK FOR ENROLLMENT ON LOCAL DATABASE
            if (jsonObject.has(EventModel.Columns.ENROLLMENT) && jsonObject.getString(EventModel.Columns.ENROLLMENT) != null) {
                Cursor cursor = briteDatabase.query(SELECT +
                                EnrollmentModel.Columns.UID +
                                FROM + EnrollmentModel.TABLE +
                                WHERE + EnrollmentModel.Columns.UID +
                                EQUAL + QUESTION_MARK,
                        jsonObject.getString(EventModel.Columns.ENROLLMENT));
                // ENROLLMENT FOUND, EVENT CAN BE SAVED
                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                    events.add(Pair.create(jsonObject.getString(EventModel.Columns.ENROLLMENT), true));
                    cursor.close();
                }
                // ENROLLMENT NOT FOUND IN LOCAL DATABASE, CHECK IF IT WAS READ FROM A QR
                else if (enrollmentJson != null) {
                    events.addAll(getEventsFromQR(jsonObject));
                }
                // ENROLLMENT NOT FOUND, EVENT CANNOT BE SAVED
                else {
                    events.add(Pair.create(jsonObject.getString(EventModel.Columns.UID), false));
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        view.renderEventInfo(events);
    }

    @Override
    public void handleRelationship(JSONArray jsonArray) {
        this.relationshipsJson.add(jsonArray);
        ArrayList<Pair<String, Boolean>> relationships = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject relationship = jsonArray.getJSONObject(i);
                relationships.add(Pair.create(relationship.getString("trackedEntityInstanceA"), true));
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        view.renderRelationship(relationships);
    }

    private void insertTEI() throws JSONException, ParseException {
        TrackedEntityInstanceModel.Builder teiModelBuilder = TrackedEntityInstanceModel.builder();
        if (teiJson.has(TrackedEntityInstanceModel.Columns.UID))
            teiModelBuilder.uid(teiJson.getString(TrackedEntityInstanceModel.Columns.UID));
        if (teiJson.has(TrackedEntityInstanceModel.Columns.CREATED))
            teiModelBuilder.created(DateUtils.databaseDateFormat().parse(teiJson.getString(TrackedEntityInstanceModel.Columns.CREATED)));
        if (teiJson.has(TrackedEntityInstanceModel.Columns.LAST_UPDATED))
            teiModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(teiJson.getString(TrackedEntityInstanceModel.Columns.LAST_UPDATED)));
        if (teiJson.has(TrackedEntityInstanceModel.Columns.STATE))
            teiModelBuilder.state(State.valueOf(teiJson.getString(TrackedEntityInstanceModel.Columns.STATE)));
        if (teiJson.has(TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT))
            teiModelBuilder.organisationUnit(teiJson.getString(TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT));
        if (teiJson.has(TrackedEntityInstanceModel.Columns.TRACKED_ENTITY_TYPE))
            teiModelBuilder.trackedEntityType(teiJson.getString(TrackedEntityInstanceModel.Columns.TRACKED_ENTITY_TYPE));

        TrackedEntityInstanceModel teiModel = teiModelBuilder.build();

        if (teiModel != null)
            briteDatabase.insert(TrackedEntityInstanceModel.TABLE, teiModel.toContentValues());
    }

    private void insertTEIAttributes(JSONObject attrV) throws JSONException, ParseException {
        TrackedEntityAttributeValueModel.Builder attrValueModelBuilder;
        attrValueModelBuilder = TrackedEntityAttributeValueModel.builder();
        if (attrV.has(TrackedEntityAttributeValueModel.Columns.CREATED))
            attrValueModelBuilder.created(DateUtils.databaseDateFormat().parse(attrV.getString(TrackedEntityAttributeValueModel.Columns.CREATED)));
        if (attrV.has(TrackedEntityAttributeValueModel.Columns.LAST_UPDATED))
            attrValueModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(attrV.getString(TrackedEntityAttributeValueModel.Columns.LAST_UPDATED)));
        if (attrV.has(TrackedEntityAttributeValueModel.Columns.VALUE))
            attrValueModelBuilder.value(attrV.getString(TrackedEntityAttributeValueModel.Columns.VALUE));
        if (attrV.has(TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE))
            attrValueModelBuilder.trackedEntityInstance(attrV.getString(TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE));
        if (attrV.has(TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE))
            attrValueModelBuilder.trackedEntityAttribute(attrV.getString(TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE));

        TrackedEntityAttributeValueModel attrValueModel = attrValueModelBuilder.build();

        if (attrValueModel != null)
            briteDatabase.insert(TrackedEntityAttributeValueModel.TABLE, attrValueModel.toContentValues());
    }

    private void insertEnrollment(JSONObject enrollment) throws JSONException, ParseException {
        EnrollmentModel.Builder enrollmentModelBuilder;
        enrollmentModelBuilder = EnrollmentModel.builder();
        if (enrollment.has(EnrollmentModel.Columns.UID))
            enrollmentModelBuilder.uid(enrollment.getString(EnrollmentModel.Columns.UID));
        if (enrollment.has(EnrollmentModel.Columns.CREATED))
            enrollmentModelBuilder.created(DateUtils.databaseDateFormat().parse(enrollment.getString(EnrollmentModel.Columns.CREATED)));
        if (enrollment.has(EnrollmentModel.Columns.LAST_UPDATED))
            enrollmentModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(enrollment.getString(EnrollmentModel.Columns.LAST_UPDATED)));
        if (enrollment.has(EnrollmentModel.Columns.STATE))
            enrollmentModelBuilder.state(State.valueOf(enrollment.getString(EnrollmentModel.Columns.STATE)));
        if (enrollment.has(EnrollmentModel.Columns.PROGRAM))
            enrollmentModelBuilder.program(enrollment.getString(EnrollmentModel.Columns.PROGRAM));
        if (enrollment.has(EnrollmentModel.Columns.FOLLOW_UP))
            enrollmentModelBuilder.followUp(enrollment.getBoolean(EnrollmentModel.Columns.FOLLOW_UP));
        if (enrollment.has(EnrollmentModel.Columns.ENROLLMENT_STATUS))
            enrollmentModelBuilder.enrollmentStatus(EnrollmentStatus.valueOf(enrollment.getString(EnrollmentModel.Columns.ENROLLMENT_STATUS)));
        if (enrollment.has(EnrollmentModel.Columns.ENROLLMENT_DATE))
            enrollmentModelBuilder.enrollmentDate(DateUtils.databaseDateFormat().parse(enrollment.getString(EnrollmentModel.Columns.ENROLLMENT_DATE)));
        if (enrollment.has(EnrollmentModel.Columns.INCIDENT_DATE))
            enrollmentModelBuilder.incidentDate(DateUtils.databaseDateFormat().parse(enrollment.getString(EnrollmentModel.Columns.INCIDENT_DATE)));
        if (enrollment.has(EnrollmentModel.Columns.ORGANISATION_UNIT))
            enrollmentModelBuilder.organisationUnit(enrollment.getString(EnrollmentModel.Columns.ORGANISATION_UNIT));
        if (enrollment.has(EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE))
            enrollmentModelBuilder.trackedEntityInstance(enrollment.getString(EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE));

        EnrollmentModel enrollmentModel = enrollmentModelBuilder.build();

        if (enrollmentModel != null)
            briteDatabase.insert(EnrollmentModel.TABLE, enrollmentModel.toContentValues());
    }

    @SuppressWarnings("squid:S3776")
    private void insertEvent(JSONObject event) throws JSONException, ParseException {
        EventModel.Builder eventModelBuilder;
        eventModelBuilder = EventModel.builder();
        if (event.has(EventModel.Columns.UID))
            eventModelBuilder.uid(event.getString(EventModel.Columns.UID));
        if (event.has(EventModel.Columns.CREATED))
            eventModelBuilder.created(DateUtils.databaseDateFormat().parse(event.getString(EventModel.Columns.CREATED)));
        if (event.has(EventModel.Columns.LAST_UPDATED))
            eventModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(event.getString(EventModel.Columns.LAST_UPDATED)));
        if (event.has(EventModel.Columns.STATE))
            eventModelBuilder.state(State.valueOf(event.getString(EventModel.Columns.STATE)));
        if (event.has(EventModel.Columns.ENROLLMENT))
            eventModelBuilder.enrollment(event.getString(EventModel.Columns.ENROLLMENT));
        if (event.has(EventModel.Columns.PROGRAM))
            eventModelBuilder.program(event.getString(EventModel.Columns.PROGRAM));
        if (event.has(EventModel.Columns.PROGRAM_STAGE))
            eventModelBuilder.programStage(event.getString(EventModel.Columns.PROGRAM_STAGE));
        if (event.has(EventModel.Columns.ORGANISATION_UNIT))
            eventModelBuilder.organisationUnit(event.getString(EventModel.Columns.ORGANISATION_UNIT));
        if (event.has(EventModel.Columns.EVENT_DATE))
            eventModelBuilder.eventDate(DateUtils.databaseDateFormat().parse(event.getString(EventModel.Columns.EVENT_DATE)));
        if (event.has(EventModel.Columns.STATUS))
            eventModelBuilder.status(EventStatus.valueOf(event.getString(EventModel.Columns.STATUS)));
        if (event.has(EventModel.Columns.ATTRIBUTE_OPTION_COMBO))
            eventModelBuilder.attributeOptionCombo(event.getString(EventModel.Columns.ATTRIBUTE_OPTION_COMBO));
        if (event.has(EventModel.Columns.TRACKED_ENTITY_INSTANCE))
            eventModelBuilder.trackedEntityInstance(event.getString(EventModel.Columns.TRACKED_ENTITY_INSTANCE));
        if (event.has(EventModel.Columns.LATITUDE))
            eventModelBuilder.latitude(event.getString(EventModel.Columns.LATITUDE));
        if (event.has(EventModel.Columns.LONGITUDE))
            eventModelBuilder.longitude(event.getString(EventModel.Columns.LONGITUDE));
        if (event.has(EventModel.Columns.COMPLETE_DATE))
            eventModelBuilder.completedDate(DateUtils.databaseDateFormat().parse(event.getString(EventModel.Columns.COMPLETE_DATE)));
        if (event.has(EventModel.Columns.DUE_DATE))
            eventModelBuilder.dueDate(DateUtils.databaseDateFormat().parse(event.getString(EventModel.Columns.DUE_DATE)));

        EventModel eventModel = eventModelBuilder.build();

        if (eventModel != null)
            briteDatabase.insert(EventModel.TABLE, eventModel.toContentValues());
    }

    private void insertTEIData(JSONObject attrV) throws JSONException, ParseException {
        TrackedEntityDataValueModel.Builder attrValueModelBuilder;
        attrValueModelBuilder = TrackedEntityDataValueModel.builder();

        if (attrV.has(TrackedEntityDataValueModel.Columns.EVENT))
            attrValueModelBuilder.event(attrV.getString(TrackedEntityDataValueModel.Columns.EVENT));
        if (attrV.has(TrackedEntityDataValueModel.Columns.LAST_UPDATED))
            attrValueModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(attrV.getString(TrackedEntityDataValueModel.Columns.LAST_UPDATED)));
        if (attrV.has(TrackedEntityDataValueModel.Columns.DATA_ELEMENT))
            attrValueModelBuilder.dataElement(attrV.getString(TrackedEntityDataValueModel.Columns.DATA_ELEMENT));
        if (attrV.has(TrackedEntityDataValueModel.Columns.STORED_BY))
            attrValueModelBuilder.storedBy(attrV.getString(TrackedEntityDataValueModel.Columns.STORED_BY));
        if (attrV.has(TrackedEntityDataValueModel.Columns.VALUE))
            attrValueModelBuilder.value(attrV.getString(TrackedEntityDataValueModel.Columns.VALUE));
        if (attrV.has(TrackedEntityDataValueModel.Columns.PROVIDED_ELSEWHERE))
            attrValueModelBuilder.providedElsewhere(Boolean.parseBoolean(attrV.getString(TrackedEntityDataValueModel.Columns.PROVIDED_ELSEWHERE)));

        TrackedEntityDataValueModel attrValueModel = attrValueModelBuilder.build();

        if (attrValueModel != null) {
            long result = briteDatabase.insert(TrackedEntityDataValueModel.TABLE, attrValueModel.toContentValues());
            Timber.d(TIMBER_MESSAGE, result);
        }
    }

    @Override
    public void init(QrReaderContracts.View view) {
        this.view = view;
    }


    private void saveTEI() {
        try {
            if (teiJson != null) {
                insertTEI();
            } else {
                view.showIdError();
            }
        } catch (JSONException | ParseException e) {
            Timber.e(e);
        }
    }

    private void saveTEIAttributes() {
        if (attrJson != null) {
            for (int i = 0; i < attrJson.size(); i++) {
                JSONArray attrArray = attrJson.get(i);
                for (int j = 0; j < attrArray.length(); j++) {
                    try {
                        JSONObject attrV = attrArray.getJSONObject(j);
                        insertTEIAttributes(attrV);
                    } catch (JSONException | ParseException e) {
                        Timber.e(e);
                    }
                }
            }
        }
    }

    private void saveEnrollment() {
        if (enrollmentJson != null) {
            for (int i = 0; i < enrollmentJson.size(); i++) {
                JSONArray enrollmentArray = enrollmentJson.get(i);
                for (int j = 0; j < enrollmentArray.length(); j++) {
                    try {
                        JSONObject enrollment = enrollmentArray.getJSONObject(j);
                        insertEnrollment(enrollment);
                    } catch (JSONException | ParseException e) {
                        Timber.e(e);
                    }
                }
            }
        }
    }

    private void saveEvents() {
        if (eventsJson != null) {
            for (int i = 0; i < eventsJson.size(); i++) {
                try {
                    JSONObject event = eventsJson.get(i);
                    insertEvent(event);
                } catch (JSONException | ParseException e) {
                    Timber.e(e);
                }
            }
        }
    }

    private void saveTEIData() {
        for (int i = 0; i < teiDataJson.size(); i++) {
            try {
                JSONObject attrV = teiDataJson.get(i);
                insertTEIData(attrV);
            } catch (JSONException | ParseException e) {
                Timber.e(e);
            }
        }
    }

    @SuppressWarnings("squid:CommentedOutCodeLine")
    private void saveRelationships() {

//TODO: CHANGE RELATIONSHIPS
//        if (relationshipsJson != null) {
//            for (int i = 0; i < relationshipsJson.size(); i++) {
//            try {
//                JSONObject relationship = relationshipsJson.getJSONObject(i);
//
//
//                RelationshipModel.Builder relationshipModelBuilder;
//                relationshipModelBuilder = RelationshipModel.builder();
//
//                if (relationship.has(TRACKED_ENTITY_INSTANCE_A))
//                    relationshipModelBuilder.trackedEntityInstanceA(relationship.getString("trackedEntityInstanceA"));
//                if (relationship.has(TRACKED_ENTITY_INSTANCE_B))
//                    relationshipModelBuilder.trackedEntityInstanceB(relationship.getString("trackedEntityInstanceB"));
//                if (relationship.has("relationshipType"))
//                    relationshipModelBuilder.relationshipType(relationship.getString("relationshipType"));
//
//                RelationshipModel relationshipModel = relationshipModelBuilder.build();
//
//                if (relationshipModel != null)
//                    briteDatabase.insert(RelationshipModel.TABLE, relationshipModel.toContentValues());
//
//            } catch (Exception e) {
//                Timber.e(e);
//            }
//            }
//        }
    }

    // SAVES READ TRACKED ENTITY INSTANCE, TRACKED ENTITY ATTRIBUTE VALUES, ENROLLMENTS, EVENTS AND RELATIONSHIPS INTO LOCAL DATABASE
    @Override
    public void download() {
        saveTEI();
        saveTEIAttributes();
        saveRelationships();
        saveEnrollment();
        saveEvents();
        saveTEIData();
        view.goToDashBoard(teiUid);
    }


    // CALLS THE ENDOPOINT TO DOWNLOAD AND SAVE THE TRACKED ENTITY INSTANCE INFO
    @Override
    public void onlineDownload() {
        view.initDownload();
        List<String> uidToDownload = new ArrayList<>();
        uidToDownload.add(teiUid);
        compositeDisposable.add(
                Observable.defer(() -> Observable.fromCallable(d2.trackedEntityModule().downloadTrackedEntityInstancesByUid(uidToDownload))).toFlowable(BackpressureStrategy.LATEST)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    view.finishDownload();
                                    if (!data.isEmpty()) {
                                        this.teiUid = data.get(0).uid();
                                        view.goToDashBoard(data.get(0).uid());
                                    } else {
                                        view.renderTeiInfo(teiUid);
                                    }
                                },
                                error -> {
                                    view.finishDownload();
                                    view.renderTeiInfo(teiUid);
                                }
                        )
        );
    }

    @Override
    public void dispose() {
        compositeDisposable.clear();
    }

    @SuppressWarnings("squid:S3776")
    private void saveEventWORegistration() throws JSONException, ParseException {
        EventModel.Builder eventModelBuilder = EventModel.builder();
        if (eventWORegistrationJson.has(EventModel.Columns.UID)) {
            eventModelBuilder.uid(eventWORegistrationJson.getString(EventModel.Columns.UID));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.ENROLLMENT)) {
            eventModelBuilder.enrollment(eventWORegistrationJson.getString(EventModel.Columns.ENROLLMENT));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.CREATED)) {
            eventModelBuilder.created(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString(EventModel.Columns.CREATED)));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.LAST_UPDATED)) {
            eventModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString(EventModel.Columns.LAST_UPDATED)));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.CREATED_AT_CLIENT)) {
            eventModelBuilder.createdAtClient(eventWORegistrationJson.getString(EventModel.Columns.CREATED_AT_CLIENT));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.LAST_UPDATED_AT_CLIENT)) {
            eventModelBuilder.lastUpdatedAtClient(eventWORegistrationJson.getString(EventModel.Columns.LAST_UPDATED_AT_CLIENT));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.STATUS)) {
            eventModelBuilder.status(EventStatus.valueOf(eventWORegistrationJson.getString(EventModel.Columns.STATUS)));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.LATITUDE)) {
            eventModelBuilder.latitude(eventWORegistrationJson.getString(EventModel.Columns.LATITUDE));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.LONGITUDE)) {
            eventModelBuilder.longitude(eventWORegistrationJson.getString(EventModel.Columns.LONGITUDE));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.PROGRAM)) {
            eventModelBuilder.program(eventWORegistrationJson.getString(EventModel.Columns.PROGRAM));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.PROGRAM_STAGE)) {
            eventModelBuilder.programStage(eventWORegistrationJson.getString(EventModel.Columns.PROGRAM_STAGE));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.ORGANISATION_UNIT)) {
            eventModelBuilder.organisationUnit(eventWORegistrationJson.getString(EventModel.Columns.ORGANISATION_UNIT));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.EVENT_DATE)) {
            eventModelBuilder.eventDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString(EventModel.Columns.EVENT_DATE)));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.COMPLETE_DATE)) {
            eventModelBuilder.completedDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString(EventModel.Columns.COMPLETE_DATE)));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.DUE_DATE)) {
            eventModelBuilder.dueDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString(EventModel.Columns.DUE_DATE)));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.ATTRIBUTE_OPTION_COMBO)) {
            eventModelBuilder.attributeOptionCombo(eventWORegistrationJson.getString(EventModel.Columns.ATTRIBUTE_OPTION_COMBO));
        }
        if (eventWORegistrationJson.has(EventModel.Columns.TRACKED_ENTITY_INSTANCE)) {
            eventModelBuilder.trackedEntityInstance(eventWORegistrationJson.getString(EventModel.Columns.TRACKED_ENTITY_INSTANCE));
        }

        eventModelBuilder.state(State.TO_UPDATE);

        EventModel eventModel = eventModelBuilder.build();

        Cursor cursor = briteDatabase.query(SELECT + ALL + FROM + EventModel.TABLE +
                WHERE + EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventModel.uid());

        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
            // EVENT ALREADY EXISTS IN THE DATABASE, JUST INSERT ATTRIBUTES
        } else {
            long result = briteDatabase.insert(EventModel.TABLE, eventModel.toContentValues());
            Timber.d(TIMBER_MESSAGE, result);
        }
    }

    @Override
    public void downloadEventWORegistration() {

        String programUid = null;
        String orgUnit = null;

        try {
            if (eventWORegistrationJson != null) {
                saveEventWORegistration();
                if (eventWORegistrationJson.has(EventModel.Columns.PROGRAM)) {
                    programUid = eventWORegistrationJson.getString(EventModel.Columns.PROGRAM);
                }
                if (eventWORegistrationJson.has(EventModel.Columns.ORGANISATION_UNIT)) {
                    orgUnit = eventWORegistrationJson.getString(EventModel.Columns.ORGANISATION_UNIT);
                }
            } else {
                view.showIdError();
                return;
            }


            for (int i = 0; i < dataJson.size(); i++) {
                JSONObject attrV = dataJson.get(i);
                insertTEIData(attrV);
            }

            view.goToEvent(eventUid, programUid, orgUnit);

        } catch (JSONException | ParseException e) {
            Timber.e(e);
        }
    }
}
