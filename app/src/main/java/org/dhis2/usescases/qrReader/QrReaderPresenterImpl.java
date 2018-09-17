package org.dhis2.usescases.qrReader;

import android.database.Cursor;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.DateUtils;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

class QrReaderPresenterImpl implements QrReaderContracts.Presenter {

    private final BriteDatabase briteDatabase;
    private final D2 d2;
    private QrReaderContracts.View view;
    private CompositeDisposable compositeDisposable;

    private JSONObject teiJson;
    private JSONArray attrJson;
    private JSONArray enrollmentJson;
    private ArrayList<JSONObject> eventJson = new ArrayList<>();
    private String teiUid;

    QrReaderPresenterImpl(BriteDatabase briteDatabase, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.d2 = d2;
        this.compositeDisposable = new CompositeDisposable();
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
            Cursor cursor = briteDatabase.query("SELECT * FROM " + TrackedEntityInstanceModel.TABLE +
                    " WHERE " + TrackedEntityInstanceModel.Columns.UID + " = ?", teiUid);
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
        this.attrJson = jsonArray;
        ArrayList<Trio<String, String, Boolean>> attributes = new ArrayList<>();
        try {
            // LOOK FOR TRACKED ENTITY ATTRIBUTES ON LOCAL DATABASE
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject attrValue = jsonArray.getJSONObject(i);
                Cursor cursor = briteDatabase.query("SELECT " +
                        TrackedEntityAttributeModel.Columns.UID + ", " +
                        TrackedEntityAttributeModel.Columns.DISPLAY_NAME +
                        " FROM " + TrackedEntityAttributeModel.TABLE +
                        " WHERE " + TrackedEntityAttributeModel.Columns.UID + " = ?",
                        attrValue.getString("trackedEntityAttribute"));
                // TRACKED ENTITY ATTRIBUTE FOUND, TRACKED ENTITY ATTRIBUTE VALUE CAN BE SAVED.
                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                    attributes.add(Trio.create(cursor.getString(1), attrValue.getString("value"), true));
                    cursor.close();
                }
                // TRACKED ENTITY ATTRIBUTE NOT FOUND, TRACKED ENTITY ATTRIBUTE VALUE CANNOT BE SAVED.
                else {
                    attributes.add(Trio.create(attrValue.getString("trackedEntityAttribute"), "", false));
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        view.renderAttrInfo(attributes);
    }

    @Override
    public void handleEnrollmentInfo(JSONArray jsonArray) {
        this.enrollmentJson = jsonArray;
        ArrayList<Pair<String, Boolean>> enrollments = new ArrayList<>();
        try {
            // LOOK FOR PROGRAM ON LOCAL DATABASE
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject attrValue = jsonArray.getJSONObject(i);
                Cursor cursor = briteDatabase.query("SELECT " +
                        ProgramModel.Columns.UID + ", " +
                        ProgramModel.Columns.DISPLAY_NAME +
                        " FROM " + ProgramModel.TABLE +
                        " WHERE " + ProgramModel.Columns.UID + " = ?",
                        attrValue.getString("program"));
                // PROGRAM FOUND, ENROLLMENT CAN BE SAVED
                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                    enrollments.add(Pair.create(cursor.getString(1), true));
                    cursor.close();
                }
                // PROGRAM NOT FOUND, ENROLLMENT CANNOT BE SAVED
                else {
                    enrollments.add(Pair.create(attrValue.getString("uid"), false));
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        view.renderEnrollmentInfo(enrollments);
    }


    @Override
    public void handleEventInfo(JSONObject jsonObject) {
        this.eventJson.add(jsonObject);
        ArrayList<Pair<String, Boolean>> events = new ArrayList<>();
        try {
            // LOOK FOR ENROLLMENT ON LOCAL DATABASE
            Cursor cursor = briteDatabase.query("SELECT " +
                    EnrollmentModel.Columns.UID +
                    " FROM " + EnrollmentModel.TABLE +
                    " WHERE " + EnrollmentModel.Columns.UID + " = ?",
                    jsonObject.getString("enrollmentUid"));
            // ENROLLMENT FOUND, EVENT CAN BE SAVED
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                events.add(Pair.create(jsonObject.getString("enrollmentUid"), true));
                cursor.close();
            }
            // ENROLLMENT NOT FOUND IN LOCAL DATABASE, CHECK IF IT WAS READ FROM A QR
            else if (enrollmentJson != null){
                boolean isEnrollmentReadFromQr = false;
                for (int i = 0; i < enrollmentJson.length(); i++) {
                    JSONObject enrollment = enrollmentJson.getJSONObject(i);
                    if (jsonObject.getString("enrollmentUid").equals(enrollment.getString(EnrollmentModel.Columns.UID))){
                        isEnrollmentReadFromQr = true;
                        break;
                    }
                }
                if (isEnrollmentReadFromQr){
                    events.add(Pair.create(jsonObject.getString("uid"), true));
                }
                else {
                    events.add(Pair.create(jsonObject.getString("uid"), false));
                }
            }
            // ENROLLMENT NOT FOUND, EVENT CANNOT BE SAVED
            else {
                events.add(Pair.create(jsonObject.getString("uid"), false));
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        view.renderEventInfo(events);
    }

    @Override
    public void init(QrReaderContracts.View view) {
        this.view = view;
    }

    // SAVES READ TRACKED ENTITY INSTANCE, TRACKED ENTITY ATTRIBUTE VALUES AND ENROLLMENTS INTO LOCAL DATABASE
    @Override
    public void download() {
        try {
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

        } catch (JSONException | ParseException e) {
            Timber.e(e);
        }

        for (int i = 0; i < attrJson.length(); i++) {
            try {
                JSONObject attrV = attrJson.getJSONObject(i);

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

        for (int i = 0; i < enrollmentJson.length(); i++) {
            try {
                JSONObject enrollment = enrollmentJson.getJSONObject(i);

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
                    enrollmentModelBuilder.incidentDate (DateUtils.databaseDateFormat().parse(enrollment.getString("incidentDate ")));
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

        for (int i = 0; i < eventJson.size(); i++) {
            try {
                JSONObject event = eventJson.get(i);

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
                if (event.has("enrollmentUid"))
                    eventModelBuilder.enrollment(event.getString("enrollmentUid"));
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
                if (event.has("attributeCategoryOptions"))
                    eventModelBuilder.attributeCategoryOptions(event.getString("attributeCategoryOptions"));
                if (event.has("attributeOptionCombo"))
                    eventModelBuilder.attributeOptionCombo(event.getString("attributeOptionCombo"));
                if (event.has("trackedEntityInstance"))
                    eventModelBuilder.trackedEntityInstance(event.getString("trackedEntityInstance"));
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
                                    if(!data.isEmpty())
                                        view.goToDashBoard(data.get(0).uid());
                                    else {
                                        view.finishDownload();
                                        view.renderTeiInfo(teiUid);
                                    }},
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
}
