package com.dhis2.usescases.qrReader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.tuples.Pair;
import com.dhis2.data.tuples.Trio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public class QrReaderContracts {

    public interface View {
        void downloadTei(@NonNull String teiUid);

        void renderTeiInfo(@Nullable String teiUid);

        void promtForMoreQr();

        void renderAttrInfo(@NonNull List<Trio<String, String, Boolean>> attributes);

        void renderEnrollmentInfo(@NonNull List<Pair<String, Boolean>> enrollments);

        void renderEventInfo(@NonNull List<Pair<String, Boolean>> events);

        void renderRelationship(@NonNull List<Pair<String, Boolean>> relationships);

        void initDownload();

        void finishDownload();

        void goToDashBoard(String teiUid, boolean isDownloadedOrPresent);
    }

    public interface Presenter {

        void handleTeiInfo(JSONObject jsonObject);

        void handleAttrInfo(JSONArray jsonArray);

        void handleEnrollmentInfo(JSONArray jsonArray);

        void handleEventInfo(JSONObject jsonObject);

        void handleRelationship(JSONArray jsonArray);

        void init(View view);

        void download();

        void onlineDownload();

        void dispose();
    }

}
