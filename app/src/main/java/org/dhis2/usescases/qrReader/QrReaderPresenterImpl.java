package org.dhis2.usescases.qrReader;

import android.util.Log;

import org.dhis2.commons.date.DateUtils;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.commons.data.tuples.Pair;
import org.dhis2.commons.data.tuples.Trio;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.event.EventTableInfo;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceTableInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static org.dhis2.commons.date.DateUtils.DATABASE_FORMAT_EXPRESSION;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

class QrReaderPresenterImpl implements QrReaderContracts.Presenter {

    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
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

    QrReaderPresenterImpl(D2 d2, SchedulerProvider schedulerProvider) {
        this.d2 = d2;
        this.compositeDisposable = new CompositeDisposable();
        this.schedulerProvider = schedulerProvider;
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
        ArrayList<Trio<TrackedEntityDataValue, String, Boolean>> attributes = new ArrayList<>();
        if (eventUid != null) {
            try {
                // LOOK FOR TRACKED ENTITY ATTRIBUTES ON LOCAL DATABASE
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject attrValue = jsonArray.getJSONObject(i);
                    TrackedEntityDataValue.Builder trackedEntityDataValueModelBuilder = TrackedEntityDataValue.builder();

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

                        if (d2.dataElementModule().dataElements().uid(attrValue.getString("dataElement")).blockingExists()) {
                            this.dataJson.add(attrValue);
                            DataElement de = d2.dataElementModule().dataElements().uid(attrValue.getString("dataElement")).blockingGet();
                            attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), de.displayFormName(), true));
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
        }

        view.renderEventDataInfo(attributes);
    }

    @Override
    public void handleDataInfo(JSONArray jsonArray) {
        ArrayList<Trio<TrackedEntityDataValue, String, Boolean>> attributes = new ArrayList<>();
        try {
            // LOOK FOR TRACKED ENTITY ATTRIBUTES ON LOCAL DATABASE
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject attrValue = jsonArray.getJSONObject(i);
                TrackedEntityDataValue.Builder trackedEntityDataValueModelBuilder = TrackedEntityDataValue.builder();

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
                    if (d2.dataElementModule().dataElements().uid(attrValue.getString("dataElement")).blockingExists()) {
                        this.teiDataJson.add(attrValue);
                        DataElement de = d2.dataElementModule().dataElements().uid(attrValue.getString("dataElement")).blockingGet();
                        attributes.add(Trio.create(trackedEntityDataValueModelBuilder.build(), de.displayFormName(), true));
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
            teiUid = jsonObject.getString("uid");
        } catch (JSONException e) {
            Timber.e(e);
        }

        // IF TEI READ
        if (teiUid != null) {
            // IF FOUND, OPEN DASHBOARD
            if (d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingExists()) {
                view.goToDashBoard(teiUid);
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
                if (attrValue.has("trackedEntityAttribute") && attrValue.getString("trackedEntityAttribute") != null) {
                    // TRACKED ENTITY ATTRIBUTE FOUND, TRACKED ENTITY ATTRIBUTE VALUE CAN BE SAVED.
                    if (d2.trackedEntityModule().trackedEntityAttributes().uid(attrValue.getString("trackedEntityAttribute")).blockingExists()) {
                        TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes().uid(attrValue.getString("trackedEntityAttribute")).blockingGet();
                        attributes.add(Trio.create(attribute.displayName(), attrValue.getString("value"), true));
                    }
                    // TRACKED ENTITY ATTRIBUTE NOT FOUND, TRACKED ENTITY ATTRIBUTE VALUE CANNOT BE SAVED.
                    else {
                        attributes.add(Trio.create(attrValue.getString("trackedEntityAttribute"), "", false));
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
                    // PROGRAM FOUND, ENROLLMENT CAN BE SAVED
                    if (d2.programModule().programs().uid(attrValue.getString("program")).blockingExists()) {
                        Program program = d2.programModule().programs().uid(attrValue.getString("program")).blockingGet();
                        enrollments.add(Pair.create(program.displayName(), true));
                    }
                    // PROGRAM NOT FOUND, ENROLLMENT CANNOT BE SAVED
                    else {
                        enrollments.add(Pair.create(attrValue.getString("uid"), false));
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
                // ENROLLMENT FOUND, EVENT CAN BE SAVED
                if (d2.enrollmentModule().enrollments().uid(jsonObject.getString("enrollment")).blockingExists()) {
                    events.add(Pair.create(jsonObject.getString("enrollment"), true));
                }
                // ENROLLMENT NOT FOUND IN LOCAL DATABASE, CHECK IF IT WAS READ FROM A QR
                else if (enrollmentJson != null) {
                    boolean isEnrollmentReadFromQr = false;
                    for (int i = 0; i < enrollmentJson.size(); i++) {
                        JSONArray enrollmentArray = enrollmentJson.get(i);
                        for (int j = 0; j < enrollmentArray.length(); j++) {
                            JSONObject enrollment = enrollmentArray.getJSONObject(j);
                            if (jsonObject.getString("enrollment").equals(enrollment.getString("uid"))) {
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
                TrackedEntityInstance.Builder teiModelBuilder = TrackedEntityInstance.builder();
                if (teiJson.has("uid"))
                    teiModelBuilder.uid(teiJson.getString("uid"));
                if (teiJson.has("created"))
                    teiModelBuilder.created(DateUtils.databaseDateFormat().parse(teiJson.getString("created")));
                if (teiJson.has("lastUpdated"))
                    teiModelBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(teiJson.getString("lastUpdated")));
                if (teiJson.has("aggregatedSyncState"))
                    teiModelBuilder.aggregatedSyncState(State.valueOf(teiJson.getString("aggregatedSyncState")));
                if (teiJson.has("organisationUnit"))
                    teiModelBuilder.organisationUnit(teiJson.getString("organisationUnit"));
                if (teiJson.has("trackedEntityType"))
                    teiModelBuilder.trackedEntityType(teiJson.getString("trackedEntityType"));
                if (teiJson.has("geometry"))
                    teiModelBuilder.geometry(
                            Geometry.builder()
                                    .type(FeatureType.valueOf(teiJson.getJSONObject("geometry").getString("type")))
                                    .coordinates(teiJson.getJSONObject("geometry").getString("coordinates"))
                                    .build()
                    );

                TrackedEntityInstance teiModel = teiModelBuilder
                        .deleted(false)
                        .build();

                if (teiModel != null) {
                    d2.databaseAdapter().insert(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), null, teiModel.toContentValues());
                }
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

                        TrackedEntityAttributeValue.Builder attrValueModelBuilder;
                        attrValueModelBuilder = TrackedEntityAttributeValue.builder();
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

                        TrackedEntityAttributeValue attrValueModel = attrValueModelBuilder.build();

                        if (attrValueModel != null)
                            d2.databaseAdapter().insert(TrackedEntityAttributeValueTableInfo.TABLE_INFO.name(), null, attrValueModel.toContentValues());

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
                        JSONObject enrollmentJson = enrollmentArray.getJSONObject(j);

                        Enrollment.Builder enrollmentBuilder;
                        enrollmentBuilder = Enrollment.builder();
                        if (enrollmentJson.has("uid"))
                            enrollmentBuilder.uid(enrollmentJson.getString("uid"));
                        if (enrollmentJson.has("created"))
                            enrollmentBuilder.created(DateUtils.databaseDateFormat().parse(enrollmentJson.getString("created")));
                        if (enrollmentJson.has("lastUpdated"))
                            enrollmentBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(enrollmentJson.getString("lastUpdated")));
                        if (enrollmentJson.has("aggregatedSyncState"))
                            enrollmentBuilder.aggregatedSyncState(State.valueOf(enrollmentJson.getString("aggregatedSyncState")));
                        if (enrollmentJson.has("program"))
                            enrollmentBuilder.program(enrollmentJson.getString("program"));
                        if (enrollmentJson.has("followUp"))
                            enrollmentBuilder.followUp(enrollmentJson.getBoolean("followUp"));
                        if (enrollmentJson.has("status"))
                            enrollmentBuilder.status(EnrollmentStatus.valueOf(enrollmentJson.getString("status")));
                        if (enrollmentJson.has("enrollmentDate"))
                            enrollmentBuilder.enrollmentDate(DateUtils.databaseDateFormat().parse(enrollmentJson.getString("enrollmentDate")));
                        if (enrollmentJson.has("incidentDate"))
                            enrollmentBuilder.incidentDate(DateUtils.databaseDateFormat().parse(enrollmentJson.getString("incidentDate")));
                        if (enrollmentJson.has("organisationUnit"))
                            enrollmentBuilder.organisationUnit(enrollmentJson.getString("organisationUnit"));
                        if (enrollmentJson.has("trackedEntityInstance"))
                            enrollmentBuilder.trackedEntityInstance(enrollmentJson.getString("trackedEntityInstance"));
                        if (enrollmentJson.has("geometry"))
                            enrollmentBuilder.geometry(
                                    Geometry.builder()
                                            .type(FeatureType.valueOf(enrollmentJson.getJSONObject("geometry").getString("type")))
                                            .coordinates(enrollmentJson.getJSONObject("geometry").getString("coordinates"))
                                            .build()
                            );

                        Enrollment enrollment = enrollmentBuilder
                                .deleted(false)
                                .build();

                        if (enrollment != null)
                            d2.databaseAdapter().insert(EnrollmentTableInfo.TABLE_INFO.name(), null, enrollment.toContentValues());

                    } catch (JSONException | ParseException e) {
                        Timber.e(e);
                    }
                }
            }
        }


        if (eventsJson != null) {
            for (int i = 0; i < eventsJson.size(); i++) {
                try {
                    JSONObject eventJson = eventsJson.get(i);

                    Event.Builder eventBuilder;
                    eventBuilder = Event.builder();
                    if (eventJson.has("uid"))
                        eventBuilder.uid(eventJson.getString("uid"));
                    if (eventJson.has("created"))
                        eventBuilder.created(DateUtils.databaseDateFormat().parse(eventJson.getString("created")));
                    if (eventJson.has("lastUpdated"))
                        eventBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(eventJson.getString("lastUpdated")));
                    if (eventJson.has("aggregatedSyncState"))
                        eventBuilder.aggregatedSyncState(State.valueOf(eventJson.getString("aggregatedSyncState")));
                    if (eventJson.has("enrollment"))
                        eventBuilder.enrollment(eventJson.getString("enrollment"));
                    if (eventJson.has("program"))
                        eventBuilder.program(eventJson.getString("program"));
                    if (eventJson.has("programStage"))
                        eventBuilder.programStage(eventJson.getString("programStage"));
                    if (eventJson.has("organisationUnit"))
                        eventBuilder.organisationUnit(eventJson.getString("organisationUnit"));
                    if (eventJson.has("eventDate"))
                        eventBuilder.eventDate(DateUtils.databaseDateFormat().parse(eventJson.getString("eventDate")));
                    if (eventJson.has("status"))
                        eventBuilder.status(EventStatus.valueOf(eventJson.getString("status")));
                    if (eventJson.has("attributeOptionCombo"))
                        eventBuilder.attributeOptionCombo(eventJson.getString("attributeOptionCombo"));
                    if (eventJson.has("geometry")) {
                        eventBuilder.geometry(
                                Geometry.builder()
                                        .type(FeatureType.valueOf(eventJson.getJSONObject("geometry").getString("type")))
                                        .coordinates(eventJson.getJSONObject("geometry").getString("coordinates"))
                                        .build()
                        );
                    }
                    if (eventJson.has("completedDate"))
                        eventBuilder.completedDate(DateUtils.databaseDateFormat().parse(eventJson.getString("completedDate")));
                    if (eventJson.has("dueDate"))
                        eventBuilder.dueDate(DateUtils.databaseDateFormat().parse(eventJson.getString("dueDate")));

                    Event eventModel = eventBuilder
                            .deleted(false)
                            .build();

                    if (eventModel != null)
                        d2.databaseAdapter().insert(EventTableInfo.TABLE_INFO.name(), null, eventModel.toContentValues());

                } catch (JSONException | ParseException e) {
                    Timber.e(e);
                }
            }
        }

        for (int i = 0; i < teiDataJson.size(); i++) {
            try {
                JSONObject attrV = teiDataJson.get(i);

                TrackedEntityDataValue.Builder attrValueModelBuilder;
                attrValueModelBuilder = TrackedEntityDataValue.builder();

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

                TrackedEntityDataValue attrValueModel = attrValueModelBuilder.build();

                if (attrValueModel != null) {
                    long result = d2.databaseAdapter().insert(TrackedEntityDataValueTableInfo.TABLE_INFO.name(), null, attrValueModel.toContentValues());
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
                d2.trackedEntityModule().trackedEntityInstanceDownloader().byUid().in(uidToDownload).download()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> {

                                },
                                error -> {
                                    view.finishDownload();
                                    view.renderTeiInfo(teiUid);
                                },
                                () -> {
                                    view.finishDownload();
                                    if (d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingExists()) {
                                        view.goToDashBoard(teiUid);
                                    } else {
                                        view.renderTeiInfo(teiUid);
                                    }
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
                Event.Builder eventBuilder = Event.builder();
                if (eventWORegistrationJson.has("uid")) {
                    eventBuilder.uid(eventWORegistrationJson.getString("uid"));
                }
                if (eventWORegistrationJson.has("enrollment")) {
                    eventBuilder.enrollment(eventWORegistrationJson.getString("enrollment"));
                }
                if (eventWORegistrationJson.has("created")) {
                    eventBuilder.created(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("created")));
                }
                if (eventWORegistrationJson.has("lastUpdated")) {
                    eventBuilder.lastUpdated(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("lastUpdated")));
                }
                if (eventWORegistrationJson.has("createdAtClient")) {
                    eventBuilder.createdAtClient(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("createdAtClient")));
                }
                if (eventWORegistrationJson.has("lastUpdatedAtClient")) {
                    eventBuilder.lastUpdatedAtClient(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("lastUpdatedAtClient")));
                }
                if (eventWORegistrationJson.has("status")) {
                    eventBuilder.status(EventStatus.valueOf(eventWORegistrationJson.getString("status")));
                }
                if (eventWORegistrationJson.has("geometry")) { //TODO: FIX QRs -> SHOULD USE SMS COMPRESSION LIBRARY
                    eventBuilder.geometry(
                            Geometry.builder()
                                    .type(FeatureType.valueOf(eventWORegistrationJson.getJSONObject("geometry").getString("type")))
                                    .coordinates(eventWORegistrationJson.getJSONObject("geometry").getString("coordinates"))
                                    .build()
                    );
                }
                if (eventWORegistrationJson.has("program")) {
                    eventBuilder.program(eventWORegistrationJson.getString("program"));
                    programUid = eventWORegistrationJson.getString("program");
                }
                if (eventWORegistrationJson.has("programStage")) {
                    eventBuilder.programStage(eventWORegistrationJson.getString("programStage"));
                }
                if (eventWORegistrationJson.has("programStage")) {
                    eventBuilder.programStage(eventWORegistrationJson.getString("programStage"));
                }
                if (eventWORegistrationJson.has("organisationUnit")) {
                    eventBuilder.organisationUnit(eventWORegistrationJson.getString("organisationUnit"));
                    orgUnit = eventWORegistrationJson.getString("organisationUnit");
                }
                if (eventWORegistrationJson.has("eventDate")) {
                    eventBuilder.eventDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("eventDate")));
                }
                if (eventWORegistrationJson.has("completedDate")) {
                    eventBuilder.completedDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("completedDate")));
                }
                if (eventWORegistrationJson.has("dueDate")) {
                    eventBuilder.dueDate(DateUtils.databaseDateFormat().parse(eventWORegistrationJson.getString("dueDate")));
                }
                if (eventWORegistrationJson.has("attributeOptionCombo")) {
                    eventBuilder.attributeOptionCombo(eventWORegistrationJson.getString("attributeOptionCombo"));
                }

                eventBuilder.aggregatedSyncState(State.TO_UPDATE);

                Event event = eventBuilder
                        .deleted(false)
                        .build();

                if (!d2.eventModule().events().uid(event.uid()).blockingExists()) {
                    long result = d2.databaseAdapter().insert(EventTableInfo.TABLE_INFO.name(), null, event.toContentValues());
                    Log.d("RESULT", "insert event " + result);
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

                TrackedEntityDataValue.Builder attrValueModelBuilder;
                attrValueModelBuilder = TrackedEntityDataValue.builder();

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

                TrackedEntityDataValue attrValueModel = attrValueModelBuilder.build();

                if (attrValueModel != null) {
                    long result = d2.databaseAdapter().insert(TrackedEntityDataValueTableInfo.TABLE_INFO.name(), null, attrValueModel.toContentValues());
                    Log.d("RESULT", "insert event " + result);
                }

            } catch (JSONException | ParseException e) {
                Timber.e(e);
            }
        }

        view.goToEvent(eventUid, programUid, orgUnit);
    }
}
