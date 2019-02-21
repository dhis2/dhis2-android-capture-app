package org.dhis2.usescases.qrReader;

import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
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
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.WHERE;
import static org.dhis2.utils.Constants.INCIDENT_DATE;
import static org.dhis2.utils.DateUtils.DATABASE_FORMAT_EXPRESSION;
import static org.hisp.dhis.android.core.enrollment.EnrollmentModel.Columns.ENROLLMENT_DATE;
import static org.hisp.dhis.android.core.enrollment.EnrollmentModel.Columns.ENROLLMENT_STATUS;
import static org.hisp.dhis.android.core.enrollment.EnrollmentModel.Columns.FOLLOW_UP;
import static org.hisp.dhis.android.core.enrollment.EnrollmentModel.Columns.LONGITUDE;
import static org.hisp.dhis.android.core.enrollment.EnrollmentModel.Columns.PROGRAM;
import static org.hisp.dhis.android.core.enrollment.EnrollmentModel.Columns.STATE;
import static org.hisp.dhis.android.core.event.EventModel.Columns.ATTRIBUTE_OPTION_COMBO;
import static org.hisp.dhis.android.core.event.EventModel.Columns.COMPLETE_DATE;
import static org.hisp.dhis.android.core.event.EventModel.Columns.CREATED_AT_CLIENT;
import static org.hisp.dhis.android.core.event.EventModel.Columns.DUE_DATE;
import static org.hisp.dhis.android.core.event.EventModel.Columns.ENROLLMENT;
import static org.hisp.dhis.android.core.event.EventModel.Columns.EVENT_DATE;
import static org.hisp.dhis.android.core.event.EventModel.Columns.LAST_UPDATED_AT_CLIENT;
import static org.hisp.dhis.android.core.event.EventModel.Columns.LATITUDE;
import static org.hisp.dhis.android.core.event.EventModel.Columns.PROGRAM_STAGE;
import static org.hisp.dhis.android.core.event.EventModel.Columns.STATUS;
import static org.hisp.dhis.android.core.event.EventModel.Columns.UID;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel.Columns.CREATED;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel.Columns.DATA_ELEMENT;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel.Columns.EVENT;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel.Columns.LAST_UPDATED;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel.Columns.PROVIDED_ELSEWHERE;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel.Columns.STORED_BY;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel.Columns.VALUE;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel.Columns.TRACKED_ENTITY_TYPE;

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
            eventUid = jsonObject.getString(UID);
        } catch (JSONException e) {
            Timber.e(e);
        }

        view.renderEventWORegistrationInfo(eventUid);
    }

    @Override
    public void handleDataWORegistrationInfo(JSONArray jsonArray) {
        ArrayList<Trio<TrackedEntityDataValueModel, String, Boolean>> attributes = new ArrayList<>();
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

            if (attrValue.has(DATA_ELEMENT) && attrValue.getString(DATA_ELEMENT) != null) {
                // LOOK FOR dataElement ON LOCAL DATABASE.
                Cursor cursor = briteDatabase.query(SELECT + ALL + FROM + DataElementModel.TABLE +
                        WHERE + DataElementModel.Columns.UID + EQUAL + QUESTION_MARK, attrValue.getString(DATA_ELEMENT));
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
        TrackedEntityDataValueModel.Builder trackedEntityDataValueModelBuilder = TrackedEntityDataValueModel.builder();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATABASE_FORMAT_EXPRESSION, Locale.getDefault());
        trackedEntityDataValueModelBuilder.event(eventUid);
        if (attrValue.has(DATA_ELEMENT)) {
            trackedEntityDataValueModelBuilder.dataElement(attrValue.getString(DATA_ELEMENT));
        }
        if (attrValue.has(STORED_BY)) {
            trackedEntityDataValueModelBuilder.storedBy(attrValue.getString(STORED_BY));
        }
        if (attrValue.has(VALUE)) {
            trackedEntityDataValueModelBuilder.value(attrValue.getString(VALUE));
        }
        if (attrValue.has(PROVIDED_ELSEWHERE)) {
            trackedEntityDataValueModelBuilder.providedElsewhere(Boolean.parseBoolean(attrValue.getString(PROVIDED_ELSEWHERE)));
        }
        if (attrValue.has(CREATED)) {
            trackedEntityDataValueModelBuilder.created(simpleDateFormat.parse(attrValue.getString(CREATED)));
        }
        if (attrValue.has(LAST_UPDATED)) {
            trackedEntityDataValueModelBuilder.lastUpdated(simpleDateFormat.parse(attrValue.getString(LAST_UPDATED)));
        }
        return trackedEntityDataValueModelBuilder;
    }

    private TrackedEntityDataValueModel.Builder parseTEDataInfoJson(JSONObject attrValue) throws JSONException, ParseException {
        TrackedEntityDataValueModel.Builder trackedEntityDataValueModelBuilder = TrackedEntityDataValueModel.builder();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATABASE_FORMAT_EXPRESSION, Locale.getDefault());

        if (attrValue.has(EVENT)) {
            trackedEntityDataValueModelBuilder.event(attrValue.getString(EVENT));
        }
        if (attrValue.has(DATA_ELEMENT)) {
            trackedEntityDataValueModelBuilder.dataElement(attrValue.getString(DATA_ELEMENT));
        }
        if (attrValue.has(STORED_BY)) {
            trackedEntityDataValueModelBuilder.storedBy(attrValue.getString(STORED_BY));
        }
        if (attrValue.has(VALUE)) {
            trackedEntityDataValueModelBuilder.value(attrValue.getString(VALUE));
        }
        if (attrValue.has(PROVIDED_ELSEWHERE)) {
            trackedEntityDataValueModelBuilder.providedElsewhere(Boolean.parseBoolean(attrValue.getString(PROVIDED_ELSEWHERE)));
        }
        if (attrValue.has(CREATED)) {
            trackedEntityDataValueModelBuilder.created(simpleDateFormat.parse(attrValue.getString(CREATED)));
        }
        if (attrValue.has(LAST_UPDATED)) {
            trackedEntityDataValueModelBuilder.lastUpdated(simpleDateFormat.parse(attrValue.getString(LAST_UPDATED)));
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

                if (attrValue.has(DATA_ELEMENT) && attrValue.getString(DATA_ELEMENT) != null) {
                    // LOOK FOR dataElement ON LOCAL DATABASE.
                    Cursor cursor = briteDatabase.query(SELECT + ALL + FROM + DataElementModel.TABLE +
                            WHERE + DataElementModel.Columns.UID + EQUAL + QUESTION_MARK, attrValue.getString(DATA_ELEMENT));
                    // IF FOUND, OPEN DASHBOARD
                    if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                        this.teiDataJson.add(attrValue);
                        attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), cursor.getString(cursor.getColumnIndex("formName")), true));
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
            teiUid = jsonObject.getString(UID);
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
                if (attrValue.has(TRACKED_ENTITY_ATTRIBUTE) && attrValue.getString(TRACKED_ENTITY_ATTRIBUTE) != null) {
                    Cursor cursor = briteDatabase.query(SELECT +
                                    TrackedEntityAttributeModel.Columns.UID + COMMA +
                                    TrackedEntityAttributeModel.Columns.DISPLAY_NAME +
                                    FROM + TrackedEntityAttributeModel.TABLE +
                                    WHERE + TrackedEntityAttributeModel.Columns.UID +
                                    EQUAL + QUESTION_MARK,
                            attrValue.getString(TRACKED_ENTITY_ATTRIBUTE));
                    // TRACKED ENTITY ATTRIBUTE FOUND, TRACKED ENTITY ATTRIBUTE VALUE CAN BE SAVED.
                    if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                        attributes.add(Trio.create(cursor.getString(1), attrValue.getString(VALUE), true));
                        cursor.close();
                    }
                    // TRACKED ENTITY ATTRIBUTE NOT FOUND, TRACKED ENTITY ATTRIBUTE VALUE CANNOT BE SAVED.
                    else {
                        attributes.add(Trio.create(attrValue.getString(TRACKED_ENTITY_ATTRIBUTE), "", false));
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
                if (attrValue.has(PROGRAM) && attrValue.getString(PROGRAM) != null) {
                    Cursor cursor = briteDatabase.query(SELECT +
                                    ProgramModel.Columns.UID + COMMA +
                                    ProgramModel.Columns.DISPLAY_NAME +
                                    FROM + ProgramModel.TABLE +
                                    WHERE + ProgramModel.Columns.UID +
                                    EQUAL + QUESTION_MARK,
                            attrValue.getString(PROGRAM));
                    // PROGRAM FOUND, ENROLLMENT CAN BE SAVED
                    if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                        enrollments.add(Pair.create(cursor.getString(1), true));
                        cursor.close();
                    }
                    // PROGRAM NOT FOUND, ENROLLMENT CANNOT BE SAVED
                    else {
                        enrollments.add(Pair.create(attrValue.getString(UID), false));
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
                if (jsonObject.getString(ENROLLMENT).equals(enrollment.getString(EnrollmentModel.Columns.UID))) {
                    isEnrollmentReadFromQr = true;
                    break;
                }
            }
        }
        if (isEnrollmentReadFromQr) {
            events.add(Pair.create(jsonObject.getString(UID), true));
        } else {
            events.add(Pair.create(jsonObject.getString(UID), false));
        }
        return events;
    }

    @Override
    public void handleEventInfo(JSONObject jsonObject) {
        this.eventsJson.add(jsonObject);
        ArrayList<Pair<String, Boolean>> events = new ArrayList<>();
        try {
            // LOOK FOR ENROLLMENT ON LOCAL DATABASE
            if (jsonObject.has(ENROLLMENT) && jsonObject.getString(ENROLLMENT) != null) {
                Cursor cursor = briteDatabase.query(SELECT +
                                EnrollmentModel.Columns.UID +
                                FROM + EnrollmentModel.TABLE +
                                WHERE + EnrollmentModel.Columns.UID +
                                EQUAL + QUESTION_MARK,
                        jsonObject.getString(ENROLLMENT));
                // ENROLLMENT FOUND, EVENT CAN BE SAVED
                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                    events.add(Pair.create(jsonObject.getString(ENROLLMENT), true));
                    cursor.close();
                }
                // ENROLLMENT NOT FOUND IN LOCAL DATABASE, CHECK IF IT WAS READ FROM A QR
                else if (enrollmentJson != null) {
                    events.addAll(getEventsFromQR(jsonObject));
                }
                // ENROLLMENT NOT FOUND, EVENT CANNOT BE SAVED
                else {
                    events.add(Pair.create(jsonObject.getString(UID), false));
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

    @Override
    public void init(QrReaderContracts.View view) {
        this.view = view;
    }

    // SAVES READ TRACKED ENTITY INSTANCE, TRACKED ENTITY ATTRIBUTE VALUES, ENROLLMENTS, EVENTS AND RELATIONSHIPS INTO LOCAL DATABASE
    @Override
    public void download() {
        try {
            if (teiJson != null) {
                TrackedEntityInstanceModel.Builder teiModelBuilder = TrackedEntityInstanceModel.builder();
                if (teiJson.has(UID))
                    teiModelBuilder.uid(teiJson.getString(UID));
                if (teiJson.has(CREATED))
                    teiModelBuilder.created(DateUtils.databaseDateFormat().parse(teiJson.getString(CREATED)));
                if (teiJson.has(LAST_UPDATED))
                    teiModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(teiJson.getString(LAST_UPDATED)));
                if (teiJson.has(STATE))
                    teiModelBuilder.state(State.valueOf(teiJson.getString(STATE)));
                if (teiJson.has(ORGANISATION_UNIT))
                    teiModelBuilder.organisationUnit(teiJson.getString(ORGANISATION_UNIT));
                if (teiJson.has(TRACKED_ENTITY_TYPE))
                    teiModelBuilder.trackedEntityType(teiJson.getString(TRACKED_ENTITY_TYPE));

                TrackedEntityInstanceModel teiModel = teiModelBuilder.build();

                if (teiModel != null)
                    briteDatabase.insert(TrackedEntityInstanceModel.TABLE, teiModel.toContentValues());
            } else {
                view.showIdError();
                return;
            }
        } catch (JSONException | ParseException e) {
            Timber.e(e);
        }

        if (attrJson != null) {
            for (int i = 0; i < attrJson.size(); i++) {
                JSONArray attrArray = attrJson.get(i);
                for (int j = 0; j < attrArray.length(); j++) {
                    try {
                        JSONObject attrV = attrArray.getJSONObject(j);

                        TrackedEntityAttributeValueModel.Builder attrValueModelBuilder;
                        attrValueModelBuilder = TrackedEntityAttributeValueModel.builder();
                        if (attrV.has(CREATED))
                            attrValueModelBuilder.created(DateUtils.databaseDateFormat().parse(attrV.getString(CREATED)));
                        if (attrV.has(LAST_UPDATED))
                            attrValueModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(attrV.getString(LAST_UPDATED)));
                        if (attrV.has(VALUE))
                            attrValueModelBuilder.value(attrV.getString(VALUE));
                        if (attrV.has(TRACKED_ENTITY_INSTANCE))
                            attrValueModelBuilder.trackedEntityInstance(attrV.getString(TRACKED_ENTITY_INSTANCE));
                        if (attrV.has(TRACKED_ENTITY_ATTRIBUTE))
                            attrValueModelBuilder.trackedEntityAttribute(attrV.getString(TRACKED_ENTITY_ATTRIBUTE));

                        TrackedEntityAttributeValueModel attrValueModel = attrValueModelBuilder.build();

                        if (attrValueModel != null)
                            briteDatabase.insert(TrackedEntityAttributeValueModel.TABLE, attrValueModel.toContentValues());

                    } catch (JSONException | ParseException e) {
                        Timber.e(e);
                    }
                }
            }
        }

        if (relationshipsJson != null) {
            for (int i = 0; i < relationshipsJson.size(); i++) {
                //TODO: CHANGE RELATIONSHIPS
            /*try {
                JSONObject relationship = relationshipsJson.getJSONObject(i);


                RelationshipModel.Builder relationshipModelBuilder;
                relationshipModelBuilder = RelationshipModel.builder();

                if (relationship.has(TRACKED_ENTITY_INSTANCE_A))
                    relationshipModelBuilder.trackedEntityInstanceA(relationship.getString("trackedEntityInstanceA"));
                if (relationship.has(TRACKED_ENTITY_INSTANCE_B))
                    relationshipModelBuilder.trackedEntityInstanceB(relationship.getString("trackedEntityInstanceB"));
                if (relationship.has("relationshipType"))
                    relationshipModelBuilder.relationshipType(relationship.getString("relationshipType"));

                RelationshipModel relationshipModel = relationshipModelBuilder.build();

                if (relationshipModel != null)
                    briteDatabase.insert(RelationshipModel.TABLE, relationshipModel.toContentValues());

            } catch (Exception e) {
                Timber.e(e);
            }*/
            }
        }

        if (enrollmentJson != null) {
            for (int i = 0; i < enrollmentJson.size(); i++) {
                JSONArray enrollmentArray = enrollmentJson.get(i);
                for (int j = 0; j < enrollmentArray.length(); j++) {
                    try {
                        JSONObject enrollment = enrollmentArray.getJSONObject(j);

                        EnrollmentModel.Builder enrollmentModelBuilder;
                        enrollmentModelBuilder = EnrollmentModel.builder();
                        if (enrollment.has(UID))
                            enrollmentModelBuilder.uid(enrollment.getString(UID));
                        if (enrollment.has(CREATED))
                            enrollmentModelBuilder.created(DateUtils.databaseDateFormat().parse(enrollment.getString(CREATED)));
                        if (enrollment.has(LAST_UPDATED))
                            enrollmentModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(enrollment.getString(LAST_UPDATED)));
                        if (enrollment.has(STATE))
                            enrollmentModelBuilder.state(State.valueOf(enrollment.getString(STATE)));
                        if (enrollment.has(PROGRAM))
                            enrollmentModelBuilder.program(enrollment.getString(PROGRAM));
                        if (enrollment.has(FOLLOW_UP))
                            enrollmentModelBuilder.followUp(enrollment.getBoolean(FOLLOW_UP));
                        if (enrollment.has(ENROLLMENT_STATUS))
                            enrollmentModelBuilder.enrollmentStatus(EnrollmentStatus.valueOf(enrollment.getString(ENROLLMENT_STATUS)));
                        if (enrollment.has(ENROLLMENT_DATE))
                            enrollmentModelBuilder.enrollmentDate(DateUtils.databaseDateFormat().parse(enrollment.getString(ENROLLMENT_DATE)));
                        if (enrollment.has(INCIDENT_DATE))
                            enrollmentModelBuilder.incidentDate(DateUtils.databaseDateFormat().parse(enrollment.getString(INCIDENT_DATE)));
                        if (enrollment.has(ORGANISATION_UNIT))
                            enrollmentModelBuilder.organisationUnit(enrollment.getString(ORGANISATION_UNIT));
                        if (enrollment.has(TRACKED_ENTITY_INSTANCE))
                            enrollmentModelBuilder.trackedEntityInstance(enrollment.getString(TRACKED_ENTITY_INSTANCE));

                        EnrollmentModel enrollmentModel = enrollmentModelBuilder.build();

                        if (enrollmentModel != null)
                            briteDatabase.insert(EnrollmentModel.TABLE, enrollmentModel.toContentValues());

                    } catch (JSONException | ParseException e) {
                        Timber.e(e);
                    }
                }
            }
        }


        if (eventsJson != null) {
            for (int i = 0; i < eventsJson.size(); i++) {
                try {
                    JSONObject event = eventsJson.get(i);

                    EventModel.Builder eventModelBuilder;
                    eventModelBuilder = EventModel.builder();
                    if (event.has(UID))
                        eventModelBuilder.uid(event.getString(UID));
                    if (event.has(CREATED))
                        eventModelBuilder.created(DateUtils.databaseDateFormat().parse(event.getString(CREATED)));
                    if (event.has(LAST_UPDATED))
                        eventModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(event.getString(LAST_UPDATED)));
                    if (event.has(STATE))
                        eventModelBuilder.state(State.valueOf(event.getString(STATE)));
                    if (event.has(ENROLLMENT))
                        eventModelBuilder.enrollment(event.getString(ENROLLMENT));
                    if (event.has(PROGRAM))
                        eventModelBuilder.program(event.getString(PROGRAM));
                    if (event.has(PROGRAM_STAGE))
                        eventModelBuilder.programStage(event.getString(PROGRAM_STAGE));
                    if (event.has(ORGANISATION_UNIT))
                        eventModelBuilder.organisationUnit(event.getString(ORGANISATION_UNIT));
                    if (event.has(EVENT_DATE))
                        eventModelBuilder.eventDate(DateUtils.databaseDateFormat().parse(event.getString(EVENT_DATE)));
                    if (event.has(STATUS))
                        eventModelBuilder.status(EventStatus.valueOf(event.getString(STATUS)));
                    if (event.has(ATTRIBUTE_OPTION_COMBO))
                        eventModelBuilder.attributeOptionCombo(event.getString(ATTRIBUTE_OPTION_COMBO));
                    if (event.has(TRACKED_ENTITY_INSTANCE))
                        eventModelBuilder.trackedEntityInstance(event.getString(TRACKED_ENTITY_INSTANCE));
                    if (event.has(LATITUDE))
                        eventModelBuilder.latitude(event.getString(LATITUDE));
                    if (event.has(LONGITUDE))
                        eventModelBuilder.longitude(event.getString(LONGITUDE));
                    if (event.has(COMPLETE_DATE))
                        eventModelBuilder.completedDate(DateUtils.databaseDateFormat().parse(event.getString(COMPLETE_DATE)));
                    if (event.has(DUE_DATE))
                        eventModelBuilder.dueDate(DateUtils.databaseDateFormat().parse(event.getString(DUE_DATE)));

                    EventModel eventModel = eventModelBuilder.build();

                    if (eventModel != null)
                        briteDatabase.insert(EventModel.TABLE, eventModel.toContentValues());

                } catch (JSONException | ParseException e) {
                    Timber.e(e);
                }
            }
        }

        for (int i = 0; i < teiDataJson.size(); i++) {
            try {
                JSONObject attrV = teiDataJson.get(i);

                TrackedEntityDataValueModel.Builder attrValueModelBuilder;
                attrValueModelBuilder = TrackedEntityDataValueModel.builder();

                if (attrV.has(EVENT))
                    attrValueModelBuilder.event(attrV.getString(EVENT));
                if (attrV.has(LAST_UPDATED))
                    attrValueModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(attrV.getString(LAST_UPDATED)));
                if (attrV.has(DATA_ELEMENT))
                    attrValueModelBuilder.dataElement(attrV.getString(DATA_ELEMENT));
                if (attrV.has(STORED_BY))
                    attrValueModelBuilder.storedBy(attrV.getString(STORED_BY));
                if (attrV.has(VALUE))
                    attrValueModelBuilder.value(attrV.getString(VALUE));
                if (attrV.has(PROVIDED_ELSEWHERE))
                    attrValueModelBuilder.providedElsewhere(Boolean.parseBoolean(attrV.getString(PROVIDED_ELSEWHERE)));

                TrackedEntityDataValueModel attrValueModel = attrValueModelBuilder.build();

                if (attrValueModel != null) {
                    long result = briteDatabase.insert(TrackedEntityDataValueModel.TABLE, attrValueModel.toContentValues());
                    Timber.d(TIMBER_MESSAGE, result);
                }

            } catch (JSONException | ParseException e) {
                Timber.e(e);
            }
        }
        view.goToDashBoard(teiUid);
    }


    // CALLS THE ENDOPOINT TO DOWNLOAD AND SAVE THE TRACKED ENTITY INSTANCE INFO
    @Override
    public void onlineDownload() {
        view.initDownload();
        List<String> uidToDownload = new ArrayList<>();
        uidToDownload.add(teiUid);
        compositeDisposable.add(
                Observable.defer(() -> Observable.fromCallable(d2.downloadTrackedEntityInstancesByUid(uidToDownload))).toFlowable(BackpressureStrategy.LATEST)
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

    @Override
    public void downloadEventWORegistration() {

        String programUid = null;
        String orgUnit = null;

        try {
            if (eventWORegistrationJson != null) {
                EventModel.Builder eventModelBuilder = EventModel.builder();
                if (eventWORegistrationJson.has(UID)) {
                    eventModelBuilder.uid(eventWORegistrationJson.getString(UID));
                }
                if (eventWORegistrationJson.has(ENROLLMENT)) {
                    eventModelBuilder.enrollment(eventWORegistrationJson.getString(ENROLLMENT));
                }
                if (eventWORegistrationJson.has(CREATED)) {
                    eventModelBuilder.created(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString(CREATED)));
                }
                if (eventWORegistrationJson.has(LAST_UPDATED)) {
                    eventModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString(LAST_UPDATED)));
                }
                if (eventWORegistrationJson.has(CREATED_AT_CLIENT)) {
                    eventModelBuilder.createdAtClient(eventWORegistrationJson.getString(CREATED_AT_CLIENT));
                }
                if (eventWORegistrationJson.has(LAST_UPDATED_AT_CLIENT)) {
                    eventModelBuilder.lastUpdatedAtClient(eventWORegistrationJson.getString(LAST_UPDATED_AT_CLIENT));
                }
                if (eventWORegistrationJson.has(STATUS)) {
                    eventModelBuilder.status(EventStatus.valueOf(eventWORegistrationJson.getString(STATUS)));
                }
                if (eventWORegistrationJson.has(LATITUDE)) {
                    eventModelBuilder.latitude(eventWORegistrationJson.getString(LATITUDE));
                }
                if (eventWORegistrationJson.has(LONGITUDE)) {
                    eventModelBuilder.longitude(eventWORegistrationJson.getString(LONGITUDE));
                }
                if (eventWORegistrationJson.has(PROGRAM)) {
                    eventModelBuilder.program(eventWORegistrationJson.getString(PROGRAM));
                    programUid = eventWORegistrationJson.getString(PROGRAM);
                }
                if (eventWORegistrationJson.has(PROGRAM_STAGE)) {
                    eventModelBuilder.programStage(eventWORegistrationJson.getString(PROGRAM_STAGE));
                }
                if (eventWORegistrationJson.has(ORGANISATION_UNIT)) {
                    eventModelBuilder.organisationUnit(eventWORegistrationJson.getString(ORGANISATION_UNIT));
                    orgUnit = eventWORegistrationJson.getString(ORGANISATION_UNIT);
                }
                if (eventWORegistrationJson.has(EVENT_DATE)) {
                    eventModelBuilder.eventDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString(EVENT_DATE)));
                }
                if (eventWORegistrationJson.has(COMPLETE_DATE)) {
                    eventModelBuilder.completedDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString(COMPLETE_DATE)));
                }
                if (eventWORegistrationJson.has(DUE_DATE)) {
                    eventModelBuilder.dueDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString(DUE_DATE)));
                }
                if (eventWORegistrationJson.has(ATTRIBUTE_OPTION_COMBO)) {
                    eventModelBuilder.attributeOptionCombo(eventWORegistrationJson.getString(ATTRIBUTE_OPTION_COMBO));
                }
                if (eventWORegistrationJson.has(TRACKED_ENTITY_INSTANCE)) {
                    eventModelBuilder.trackedEntityInstance(eventWORegistrationJson.getString(TRACKED_ENTITY_INSTANCE));
                }

                eventModelBuilder.state(State.TO_UPDATE);

                EventModel eventModel = eventModelBuilder.build();

                Cursor cursor = briteDatabase.query(SELECT + ALL + FROM + EventModel.TABLE +
                        WHERE + UID + EQUAL + QUESTION_MARK, eventModel.uid());

                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                    // EVENT ALREADY EXISTS IN THE DATABASE, JUST INSERT ATTRIBUTES
                } else {
                    long result = briteDatabase.insert(EventModel.TABLE, eventModel.toContentValues());
                    Timber.d(TIMBER_MESSAGE, result);
                }
            } else {
                view.showIdError();
                return;
            }
        } catch (JSONException | ParseException e) {
            Timber.e(e);
        }


        for (int i = 0; i < dataJson.size(); i++) {
            try {
                JSONObject attrV = dataJson.get(i);

                TrackedEntityDataValueModel.Builder attrValueModelBuilder;
                attrValueModelBuilder = TrackedEntityDataValueModel.builder();

                if (attrV.has(EVENT))
                    attrValueModelBuilder.event(attrV.getString(EVENT));
                if (attrV.has(LAST_UPDATED))
                    attrValueModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(attrV.getString(LAST_UPDATED)));
                if (attrV.has(DATA_ELEMENT))
                    attrValueModelBuilder.dataElement(attrV.getString(DATA_ELEMENT));
                if (attrV.has(STORED_BY))
                    attrValueModelBuilder.storedBy(attrV.getString(STORED_BY));
                if (attrV.has(VALUE))
                    attrValueModelBuilder.value(attrV.getString(VALUE));
                if (attrV.has(PROVIDED_ELSEWHERE))
                    attrValueModelBuilder.providedElsewhere(Boolean.parseBoolean(attrV.getString(PROVIDED_ELSEWHERE)));

                TrackedEntityDataValueModel attrValueModel = attrValueModelBuilder.build();

                if (attrValueModel != null) {
                    long result = briteDatabase.insert(TrackedEntityDataValueModel.TABLE, attrValueModel.toContentValues());
                    Timber.d(TIMBER_MESSAGE, result);
                }

            } catch (JSONException | ParseException e) {
                Timber.e(e);
            }
        }

        view.goToEvent(eventUid, programUid, orgUnit);
    }
}
