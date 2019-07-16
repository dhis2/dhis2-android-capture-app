package org.dhis2.usescases.qrReader;

import android.database.Cursor;
import android.util.Log;

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
            eventUid = jsonObject.getString("uid");
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
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject attrValue = jsonArray.getJSONObject(i);
                    TrackedEntityDataValueModel.Builder trackedEntityDataValueModelBuilder = TrackedEntityDataValueModel.builder();

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATABASE_FORMAT_EXPRESSION, Locale.getDefault());
                    trackedEntityDataValueModelBuilder.event(eventUid);
                    if (attrValue.has("dataElement")) {
                        trackedEntityDataValueModelBuilder.dataElement(attrValue.getString("dataElement"));
                    }
                    if (attrValue.has("storedBy")) {
                        trackedEntityDataValueModelBuilder.storedBy(attrValue.getString("storedBy"));
                    }
                    if (attrValue.has("value")) {
                        trackedEntityDataValueModelBuilder.value(attrValue.getString("value"));
                    }
                    if (attrValue.has("providedElsewhere")) {
                        trackedEntityDataValueModelBuilder.providedElsewhere(Boolean.parseBoolean(attrValue.getString("providedElsewhere")));
                    }
                    if (attrValue.has("created")) {
                        trackedEntityDataValueModelBuilder.created(simpleDateFormat.parse(attrValue.getString("created")));
                    }
                    if (attrValue.has("lastUpdated")) {
                        trackedEntityDataValueModelBuilder.lastUpdated(simpleDateFormat.parse(attrValue.getString("lastUpdated")));
                    }

                    if (attrValue.has("dataElement") && attrValue.getString("dataElement") != null) {
                        // LOOK FOR dataElement ON LOCAL DATABASE.
                        // IF FOUND, OPEN DASHBOARD
                        try (Cursor cursor = briteDatabase.query("SELECT * FROM " + DataElementModel.TABLE +
                                " WHERE " + DataElementModel.Columns.UID + " = ?", attrValue.getString("dataElement"))) {
                            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                                this.dataJson.add(attrValue);
                                attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), cursor.getString(cursor.getColumnIndex("formName")), true));
                            } else {
                                attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), null, false));
                            }
                        }
                    } else {
                        attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), null, false));
                    }
                }
            } catch (JSONException | ParseException e) {
                Timber.e(e);
            }
        }

        view.renderEventDataInfo(attributes);
    }

    @Override
    public void handleDataInfo(JSONArray jsonArray) {
        ArrayList<Trio<TrackedEntityDataValueModel, String, Boolean>> attributes = new ArrayList<>();
        try {
            // LOOK FOR TRACKED ENTITY ATTRIBUTES ON LOCAL DATABASE
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject attrValue = jsonArray.getJSONObject(i);
                TrackedEntityDataValueModel.Builder trackedEntityDataValueModelBuilder = TrackedEntityDataValueModel.builder();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATABASE_FORMAT_EXPRESSION, Locale.getDefault());

                if (attrValue.has("event")) {
                    trackedEntityDataValueModelBuilder.event(attrValue.getString("event"));
                }
                if (attrValue.has("dataElement")) {
                    trackedEntityDataValueModelBuilder.dataElement(attrValue.getString("dataElement"));
                }
                if (attrValue.has("storedBy")) {
                    trackedEntityDataValueModelBuilder.storedBy(attrValue.getString("storedBy"));
                }
                if (attrValue.has("value")) {
                    trackedEntityDataValueModelBuilder.value(attrValue.getString("value"));
                }
                if (attrValue.has("providedElsewhere")) {
                    trackedEntityDataValueModelBuilder.providedElsewhere(Boolean.parseBoolean(attrValue.getString("providedElsewhere")));
                }
                if (attrValue.has("created")) {
                    trackedEntityDataValueModelBuilder.created(simpleDateFormat.parse(attrValue.getString("created")));
                }
                if (attrValue.has("lastUpdated")) {
                    trackedEntityDataValueModelBuilder.lastUpdated(simpleDateFormat.parse(attrValue.getString("lastUpdated")));
                }

                if (attrValue.has("dataElement") && attrValue.getString("dataElement") != null) {
                    // LOOK FOR dataElement ON LOCAL DATABASE.
                    // IF FOUND, OPEN DASHBOARD
                    try (Cursor cursor = briteDatabase.query("SELECT * FROM " + DataElementModel.TABLE +
                            " WHERE " + DataElementModel.Columns.UID + " = ?", attrValue.getString("dataElement"))) {
                        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                            this.teiDataJson.add(attrValue);
                            attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), cursor.getString(cursor.getColumnIndex("formName")), true));
                        } else {
                            attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), null, false));
                        }
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
            teiUid = jsonObject.getString("uid");
        } catch (JSONException e) {
            Timber.e(e);
        }

        // IF TEI READ
        if (teiUid != null) {
            // LOOK FOR TEI ON LOCAL DATABASE.
            try (Cursor cursor = briteDatabase.query("SELECT * FROM " + TrackedEntityInstanceModel.TABLE +
                    " WHERE " + TrackedEntityInstanceModel.Columns.UID + " = ?", teiUid)) {
                // IF FOUND, OPEN DASHBOARD
                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                    view.goToDashBoard(teiUid);
                }
                // IF NOT FOUND, TRY TO DOWNLOAD ONLINE, OR PROMPT USER TO SCAN MORE QR CODES
                else {
                    view.downloadTei(teiUid);
                }
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
                if (attrValue.has("trackedEntityAttribute") && attrValue.getString("trackedEntityAttribute") != null) {
                    try (Cursor cursor = briteDatabase.query("SELECT " +
                                    TrackedEntityAttributeModel.Columns.UID + ", " +
                                    TrackedEntityAttributeModel.Columns.DISPLAY_NAME +
                                    " FROM " + TrackedEntityAttributeModel.TABLE +
                                    " WHERE " + TrackedEntityAttributeModel.Columns.UID + " = ?",
                            attrValue.getString("trackedEntityAttribute"))) {
                        // TRACKED ENTITY ATTRIBUTE FOUND, TRACKED ENTITY ATTRIBUTE VALUE CAN BE SAVED.
                        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                            attributes.add(Trio.create(cursor.getString(1), attrValue.getString("value"), true));
                        }
                        // TRACKED ENTITY ATTRIBUTE NOT FOUND, TRACKED ENTITY ATTRIBUTE VALUE CANNOT BE SAVED.
                        else {
                            attributes.add(Trio.create(attrValue.getString("trackedEntityAttribute"), "", false));
                        }
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
                if (attrValue.has("program") && attrValue.getString("program") != null) {
                    try (Cursor cursor = briteDatabase.query("SELECT " +
                                    ProgramModel.Columns.UID + ", " +
                                    ProgramModel.Columns.DISPLAY_NAME +
                                    " FROM " + ProgramModel.TABLE +
                                    " WHERE " + ProgramModel.Columns.UID + " = ?",
                            attrValue.getString("program"))) {
                        // PROGRAM FOUND, ENROLLMENT CAN BE SAVED
                        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                            enrollments.add(Pair.create(cursor.getString(1), true));
                        }
                        // PROGRAM NOT FOUND, ENROLLMENT CANNOT BE SAVED
                        else {
                            enrollments.add(Pair.create(attrValue.getString("uid"), false));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        view.renderEnrollmentInfo(enrollments);
    }


    @Override
    public void handleEventInfo(JSONObject jsonObject) {
        this.eventsJson.add(jsonObject);
        ArrayList<Pair<String, Boolean>> events = new ArrayList<>();
        try {
            // LOOK FOR ENROLLMENT ON LOCAL DATABASE
            if (jsonObject.has("enrollment") && jsonObject.getString("enrollment") != null) {
                try (Cursor cursor = briteDatabase.query("SELECT " +
                                EnrollmentModel.Columns.UID +
                                " FROM " + EnrollmentModel.TABLE +
                                " WHERE " + EnrollmentModel.Columns.UID + " = ?",
                        jsonObject.getString("enrollment"))) {
                    // ENROLLMENT FOUND, EVENT CAN BE SAVED
                    if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                        events.add(Pair.create(jsonObject.getString("enrollment"), true));
                    }
                    // ENROLLMENT NOT FOUND IN LOCAL DATABASE, CHECK IF IT WAS READ FROM A QR
                    else if (enrollmentJson != null) {
                        boolean isEnrollmentReadFromQr = false;
                        for (int i = 0; i < enrollmentJson.size(); i++) {
                            JSONArray enrollmentArray = enrollmentJson.get(i);
                            for (int j = 0; j < enrollmentArray.length(); j++) {
                                JSONObject enrollment = enrollmentArray.getJSONObject(j);
                                if (jsonObject.getString("enrollment").equals(enrollment.getString(EnrollmentModel.Columns.UID))) {
                                    isEnrollmentReadFromQr = true;
                                    break;
                                }
                            }
                        }
                        if (isEnrollmentReadFromQr) {
                            events.add(Pair.create(jsonObject.getString("uid"), true));
                        } else {
                            events.add(Pair.create(jsonObject.getString("uid"), false));
                        }
                    }
                    // ENROLLMENT NOT FOUND, EVENT CANNOT BE SAVED
                    else {
                        events.add(Pair.create(jsonObject.getString("uid"), false));
                    }
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
                if (teiJson.has("uid"))
                    teiModelBuilder.uid(teiJson.getString("uid"));
                if (teiJson.has("created"))
                    teiModelBuilder.created(DateUtils.databaseDateFormat().parse(teiJson.getString("created")));
                if (teiJson.has("lastUpdated"))
                    teiModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(teiJson.getString("lastUpdated")));
                if (teiJson.has("state"))
                    teiModelBuilder.state(State.valueOf(teiJson.getString("state")));
                if (teiJson.has("organisationUnit"))
                    teiModelBuilder.organisationUnit(teiJson.getString("organisationUnit"));
                if (teiJson.has("trackedEntityType"))
                    teiModelBuilder.trackedEntityType(teiJson.getString("trackedEntityType"));

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
                        if (attrV.has("created"))
                            attrValueModelBuilder.created(DateUtils.databaseDateFormat().parse(attrV.getString("created")));
                        if (attrV.has("lastUpdated"))
                            attrValueModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(attrV.getString("lastUpdated")));
                        if (attrV.has("value"))
                            attrValueModelBuilder.value(attrV.getString("value"));
                        if (attrV.has("trackedEntityInstance"))
                            attrValueModelBuilder.trackedEntityInstance(attrV.getString("trackedEntityInstance"));
                        if (attrV.has("trackedEntityAttribute"))
                            attrValueModelBuilder.trackedEntityAttribute(attrV.getString("trackedEntityAttribute"));

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

                if (relationship.has("trackedEntityInstanceA"))
                    relationshipModelBuilder.trackedEntityInstanceA(relationship.getString("trackedEntityInstanceA"));
                if (relationship.has("trackedEntityInstanceB"))
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
                        if (enrollment.has("uid"))
                            enrollmentModelBuilder.uid(enrollment.getString("uid"));
                        if (enrollment.has("created"))
                            enrollmentModelBuilder.created(DateUtils.databaseDateFormat().parse(enrollment.getString("created")));
                        if (enrollment.has("lastUpdated"))
                            enrollmentModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(enrollment.getString("lastUpdated")));
                        if (enrollment.has("state"))
                            enrollmentModelBuilder.state(State.valueOf(enrollment.getString("state")));
                        if (enrollment.has("program"))
                            enrollmentModelBuilder.program(enrollment.getString("program"));
                        if (enrollment.has("followUp"))
                            enrollmentModelBuilder.followUp(enrollment.getBoolean("followUp"));
                        if (enrollment.has("enrollmentStatus"))
                            enrollmentModelBuilder.enrollmentStatus(EnrollmentStatus.valueOf(enrollment.getString("enrollmentStatus")));
                        if (enrollment.has("enrollmentDate"))
                            enrollmentModelBuilder.enrollmentDate(DateUtils.databaseDateFormat().parse(enrollment.getString("enrollmentDate")));
                        if (enrollment.has("dateOfIncident"))
                            enrollmentModelBuilder.incidentDate(DateUtils.databaseDateFormat().parse(enrollment.getString("incidentDate ")));
                        if (enrollment.has("organisationUnit"))
                            enrollmentModelBuilder.organisationUnit(enrollment.getString("organisationUnit"));
                        if (enrollment.has("trackedEntityInstance"))
                            enrollmentModelBuilder.trackedEntityInstance(enrollment.getString("trackedEntityInstance"));

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
                    if (event.has("uid"))
                        eventModelBuilder.uid(event.getString("uid"));
                    if (event.has("created"))
                        eventModelBuilder.created(DateUtils.databaseDateFormat().parse(event.getString("created")));
                    if (event.has("lastUpdated"))
                        eventModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(event.getString("lastUpdated")));
                    if (event.has("state"))
                        eventModelBuilder.state(State.valueOf(event.getString("state")));
                    if (event.has("enrollment"))
                        eventModelBuilder.enrollment(event.getString("enrollment"));
                    if (event.has("program"))
                        eventModelBuilder.program(event.getString("program"));
                    if (event.has("programStage"))
                        eventModelBuilder.programStage(event.getString("programStage"));
                    if (event.has("organisationUnit"))
                        eventModelBuilder.organisationUnit(event.getString("organisationUnit"));
                    if (event.has("eventDate"))
                        eventModelBuilder.eventDate(DateUtils.databaseDateFormat().parse(event.getString("eventDate")));
                    if (event.has("status"))
                        eventModelBuilder.status(EventStatus.valueOf(event.getString("status")));
                    if (event.has("attributeOptionCombo"))
                        eventModelBuilder.attributeOptionCombo(event.getString("attributeOptionCombo"));
                    if (event.has("latitude"))
                        eventModelBuilder.latitude(event.getString("latitude"));
                    if (event.has("longitude"))
                        eventModelBuilder.longitude(event.getString("longitude"));
                    if (event.has("completedDate"))
                        eventModelBuilder.completedDate(DateUtils.databaseDateFormat().parse(event.getString("completedDate")));
                    if (event.has("dueDate"))
                        eventModelBuilder.dueDate(DateUtils.databaseDateFormat().parse(event.getString("dueDate")));

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

                if (attrV.has("event"))
                    attrValueModelBuilder.event(attrV.getString("event"));
                if (attrV.has("lastUpdated"))
                    attrValueModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(attrV.getString("lastUpdated")));
                if (attrV.has("dataElement"))
                    attrValueModelBuilder.dataElement(attrV.getString("dataElement"));
                if (attrV.has("storedBy"))
                    attrValueModelBuilder.storedBy(attrV.getString("storedBy"));
                if (attrV.has("value"))
                    attrValueModelBuilder.value(attrV.getString("value"));
                if (attrV.has("providedElsewhere"))
                    attrValueModelBuilder.providedElsewhere(Boolean.parseBoolean(attrV.getString("providedElsewhere")));

                TrackedEntityDataValueModel attrValueModel = attrValueModelBuilder.build();

                if (attrValueModel != null) {
                    long result = briteDatabase.insert(TrackedEntityDataValueModel.TABLE, attrValueModel.toContentValues());
                    Log.d("RESULT", "insert event " + result);
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

    @Override
    public void downloadEventWORegistration() {

        String programUid = null;
        String orgUnit = null;

        try {
            if (eventWORegistrationJson != null) {
                EventModel.Builder eventModelBuilder = EventModel.builder();
                if (eventWORegistrationJson.has("uid")) {
                    eventModelBuilder.uid(eventWORegistrationJson.getString("uid"));
                }
                if (eventWORegistrationJson.has("enrollment")) {
                    eventModelBuilder.enrollment(eventWORegistrationJson.getString("enrollment"));
                }
                if (eventWORegistrationJson.has("created")) {
                    eventModelBuilder.created(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("created")));
                }
                if (eventWORegistrationJson.has("lastUpdated")) {
                    eventModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("lastUpdated")));
                }
                if (eventWORegistrationJson.has("createdAtClient")) {
                    eventModelBuilder.createdAtClient(eventWORegistrationJson.getString("createdAtClient"));
                }
                if (eventWORegistrationJson.has("lastUpdatedAtClient")) {
                    eventModelBuilder.lastUpdatedAtClient(eventWORegistrationJson.getString("lastUpdatedAtClient"));
                }
                if (eventWORegistrationJson.has("status")) {
                    eventModelBuilder.status(EventStatus.valueOf(eventWORegistrationJson.getString("status")));
                }
                if (eventWORegistrationJson.has("latitude")) {
                    eventModelBuilder.latitude(eventWORegistrationJson.getString("latitude"));
                }
                if (eventWORegistrationJson.has("longitude")) {
                    eventModelBuilder.longitude(eventWORegistrationJson.getString("longitude"));
                }
                if (eventWORegistrationJson.has("program")) {
                    eventModelBuilder.program(eventWORegistrationJson.getString("program"));
                    programUid = eventWORegistrationJson.getString("program");
                }
                if (eventWORegistrationJson.has("programStage")) {
                    eventModelBuilder.programStage(eventWORegistrationJson.getString("programStage"));
                }
                if (eventWORegistrationJson.has("programStage")) {
                    eventModelBuilder.programStage(eventWORegistrationJson.getString("programStage"));
                }
                if (eventWORegistrationJson.has("organisationUnit")) {
                    eventModelBuilder.organisationUnit(eventWORegistrationJson.getString("organisationUnit"));
                    orgUnit = eventWORegistrationJson.getString("organisationUnit");
                }
                if (eventWORegistrationJson.has("eventDate")) {
                    eventModelBuilder.eventDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("eventDate")));
                }
                if (eventWORegistrationJson.has("completedDate")) {
                    eventModelBuilder.completedDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("completedDate")));
                }
                if (eventWORegistrationJson.has("dueDate")) {
                    eventModelBuilder.dueDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("dueDate")));
                }
                if (eventWORegistrationJson.has("attributeOptionCombo")) {
                    eventModelBuilder.attributeOptionCombo(eventWORegistrationJson.getString("attributeOptionCombo"));
                }

                eventModelBuilder.state(State.TO_UPDATE);

                EventModel eventModel = eventModelBuilder.build();

                try (Cursor cursor = briteDatabase.query("SELECT * FROM " + EventModel.TABLE +
                        " WHERE " + EventModel.Columns.UID + " = ?", eventModel.uid())) {
                    if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                        // EVENT ALREADY EXISTS IN THE DATABASE, JUST INSERT ATTRIBUTES
                    } else {
                        long result = briteDatabase.insert(EventModel.TABLE, eventModel.toContentValues());
                        Log.d("RESULT", "insert event " + result);
                    }
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

                if (attrV.has("event"))
                    attrValueModelBuilder.event(attrV.getString("event"));
                if (attrV.has("lastUpdated"))
                    attrValueModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(attrV.getString("lastUpdated")));
                if (attrV.has("dataElement"))
                    attrValueModelBuilder.dataElement(attrV.getString("dataElement"));
                if (attrV.has("storedBy"))
                    attrValueModelBuilder.storedBy(attrV.getString("storedBy"));
                if (attrV.has("value"))
                    attrValueModelBuilder.value(attrV.getString("value"));
                if (attrV.has("providedElsewhere"))
                    attrValueModelBuilder.providedElsewhere(Boolean.parseBoolean(attrV.getString("providedElsewhere")));

                TrackedEntityDataValueModel attrValueModel = attrValueModelBuilder.build();

                if (attrValueModel != null) {
                    long result = briteDatabase.insert(TrackedEntityDataValueModel.TABLE, attrValueModel.toContentValues());
                    Log.d("RESULT", "insert event " + result);
                }

            } catch (JSONException | ParseException e) {
                Timber.e(e);
            }
        }

        view.goToEvent(eventUid, programUid, orgUnit);
    }
}
