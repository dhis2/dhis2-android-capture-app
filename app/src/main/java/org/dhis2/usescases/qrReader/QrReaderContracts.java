package org.dhis2.usescases.qrReader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public class QrReaderContracts {

    public interface View {
        void downloadEventWORegistration(@NonNull String eventUid);

        void renderEventWORegistrationInfo(@Nullable String eventUid);

        void downloadTei(@NonNull String teiUid);

        void renderTeiInfo(@Nullable String teiUid);

        void promtForTEIMoreQr();

        void promtForEventWORegistrationMoreQr();

        void renderEventDataInfo(@NonNull List<Trio<TrackedEntityDataValue, String, Boolean>> data);

        void renderTeiEventDataInfo(@NonNull List<Trio<TrackedEntityDataValue, String, Boolean>> data);

        void renderAttrInfo(@NonNull List<Trio<String, String, Boolean>> attributes);

        void renderEnrollmentInfo(@NonNull List<Pair<String, Boolean>> enrollments);

        void renderEventInfo(@NonNull List<Pair<String, Boolean>> events);

        void renderRelationship(@NonNull List<Pair<String, Boolean>> relationships);

        void initDownload();

        void finishDownload();

        void goToDashBoard(String teiUid);

        void goToEvent(String eventUid, String programId, String orgUnit);

        void showIdError();
    }

    public interface Presenter {

        void handleEventWORegistrationInfo(JSONObject jsonObject);

        void handleDataWORegistrationInfo(JSONArray jsonArray);

        void handleDataInfo(JSONArray jsonArray);

        void handleTeiInfo(JSONObject jsonObject);

        void handleAttrInfo(JSONArray jsonArray);

        void handleEnrollmentInfo(JSONArray jsonArray);

        void handleEventInfo(JSONObject jsonObject);

        void handleRelationship(JSONArray jsonArray);

        void init(View view);

        void download();

        void downloadEventWORegistration();

        void onlineDownload();

        void dispose();
    }

}
