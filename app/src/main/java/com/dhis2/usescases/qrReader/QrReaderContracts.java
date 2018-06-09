package com.dhis2.usescases.qrReader;

import com.dhis2.data.tuples.Pair;
import com.dhis2.data.tuples.Trio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public class QrReaderContracts {

    public interface View {
        void renderTeiInfo(String info, boolean isOk);

        void renderAttrInfo(ArrayList<Trio<String, String, Boolean>> attributes);

        void renderEnrollmentInfo(ArrayList<Pair<String, Boolean>> attributes);

        void initDownload();

        void goToDashBoard(String uid);
    }

    public interface Presenter {

        void handleTeiInfo(JSONObject jsonObject);

        void handleAttrInfo(JSONArray jsonArray);

        void handleEnrollmentInfo(JSONArray jsonArray);

        void init(View view);

        void download();

        void onlineDownload();

        void dispose();
    }

}
