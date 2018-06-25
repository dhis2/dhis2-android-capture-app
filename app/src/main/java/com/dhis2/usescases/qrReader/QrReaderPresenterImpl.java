package com.dhis2.usescases.qrReader;

import android.database.Cursor;

import com.dhis2.data.tuples.Pair;
import com.dhis2.data.tuples.Trio;
import com.dhis2.utils.DateUtils;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
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
            e.printStackTrace();
        }

        if (teiUid != null) {
            Cursor cursor = briteDatabase.query("SELECT * FROM TrackedEntityInstance WHERE TrackedEntityInstance.uid = ?", teiUid);
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                view.goToDashBoard(teiUid);
                cursor.close();
            } else
                view.renderTeiInfo("Read next QR", true);
        } else
            view.renderTeiInfo("This QR is not from a trackedEntityInstance", false);
    }

    @Override
    public void handleAttrInfo(JSONArray jsonArray) {
        this.attrJson = jsonArray;
        ArrayList<Trio<String, String, Boolean>> attributes = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject attrValue = jsonArray.getJSONObject(i);
                Cursor cursor = briteDatabase.query("SELECT TrackedEntityAttribute.uid, TrackedEntityAttribute.displayName FROM TrackedEntityAttribute WHERE TrackedEntityAttribute.uid = ?", attrValue.getString("trackedEntityAttribute"));
                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                    attributes.add(Trio.create(cursor.getString(1), attrValue.getString("value"), true)); //attribute found. Can be locally saved
                    cursor.close();
                } else
                    attributes.add(Trio.create(attrValue.getString("trackedEntityAttribute"), "", false)); //attribute not found. Cant be saved

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        view.renderAttrInfo(attributes);
    }

    @Override
    public void handleEnrollmentInfo(JSONArray jsonArray) {
        this.enrollmentJson = jsonArray;
        ArrayList<Pair<String, Boolean>> enrollents = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject attrValue = jsonArray.getJSONObject(i);
                Cursor cursor = briteDatabase.query("SELECT Program.uid, Program.displayName FROM Program WHERE Program.uid = ?", attrValue.getString("program"));
                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                    enrollents.add(Pair.create(cursor.getString(1), true)); //Program found. Can be saved
                    cursor.close();
                } else
                    enrollents.add(Pair.create(attrValue.getString("uid"), false)); //Program not found. Cant be saved

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        view.renderEnrollmentInfo(enrollents);

    }

    @Override
    public void init(QrReaderContracts.View view) {
        this.view = view;
    }

    @Override
    public void download() {

        view.initDownload();

        TrackedEntityInstanceModel teiModel = null;
        try {
            teiModel = TrackedEntityInstanceModel.builder()
                    .uid(teiJson.getString(TrackedEntityInstanceModel.Columns.UID))
                    .created(DateUtils.databaseDateFormat().parse(teiJson.getString(TrackedEntityInstanceModel.Columns.CREATED)))
                    .lastUpdated(DateUtils.databaseDateFormat().parse(teiJson.getString(TrackedEntityInstanceModel.Columns.LAST_UPDATED)))
                    .organisationUnit(teiJson.getString(TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT))
                    .state(State.valueOf(teiJson.getString(TrackedEntityInstanceModel.Columns.STATE)))
                    .trackedEntityType(teiJson.getString(TrackedEntityInstanceModel.Columns.TRACKED_ENTITY_TYPE))
                    .build();
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        if (teiModel != null)
            briteDatabase.insert(TrackedEntityInstanceModel.TABLE, teiModel.toContentValues());

        for (int i = 0; i < attrJson.length(); i++) {
            try {
                JSONObject attrV = attrJson.getJSONObject(i);

                TrackedEntityAttributeValueModel attrValueModel;
                attrValueModel = TrackedEntityAttributeValueModel.builder()
                        .value(attrV.getString(TrackedEntityAttributeValueModel.Columns.VALUE))
                        .created(DateUtils.databaseDateFormat().parse(attrV.getString(TrackedEntityAttributeValueModel.Columns.CREATED)))
                        .lastUpdated(DateUtils.databaseDateFormat().parse(attrV.getString(TrackedEntityAttributeValueModel.Columns.LAST_UPDATED)))
                        .trackedEntityInstance(attrV.getString(TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE))
                        .trackedEntityAttribute(attrV.getString(TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE))
                        .build();

                if (attrValueModel != null)
                    briteDatabase.insert(TrackedEntityInstanceModel.TABLE, attrValueModel.toContentValues());

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < enrollmentJson.length(); i++) {
            try {
                JSONObject enrollment = enrollmentJson.getJSONObject(i);

                EnrollmentModel enrollmentModel;
                enrollmentModel = EnrollmentModel.builder()
                        .followUp(enrollment.getBoolean(EnrollmentModel.Columns.FOLLOW_UP))
                        .created(DateUtils.databaseDateFormat().parse(enrollment.getString(EnrollmentModel.Columns.CREATED)))
                        .enrollmentStatus(EnrollmentStatus.valueOf(enrollment.getString(EnrollmentModel.Columns.ENROLLMENT_STATUS)))
                        .dateOfEnrollment(DateUtils.databaseDateFormat().parse(enrollment.getString(EnrollmentModel.Columns.DATE_OF_ENROLLMENT)))
                        .dateOfIncident(DateUtils.databaseDateFormat().parse(enrollment.getString(EnrollmentModel.Columns.DATE_OF_INCIDENT)))
                        .lastUpdated(DateUtils.databaseDateFormat().parse(enrollment.getString(EnrollmentModel.Columns.LAST_UPDATED)))
                        .organisationUnit(enrollment.getString(EnrollmentModel.Columns.ORGANISATION_UNIT))
                        .uid(enrollment.getString(EnrollmentModel.Columns.UID))
                        .trackedEntityInstance(enrollment.getString(EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE))
                        .build();

                if (enrollmentModel != null)
                    briteDatabase.insert(EnrollmentModel.TABLE, enrollmentModel.toContentValues());

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }

        view.goToDashBoard(teiUid);
    }

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
                                    else
                                        view.emptyOnlineData();
                                    },
                                Timber::d
                        )
        );

    }

    @Override
    public void dispose() {
        compositeDisposable.clear();
    }
}
