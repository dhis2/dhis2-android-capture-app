package org.dhis2.data.qr;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public class QRjson {

    public static final String EVENT_JSON = "EVENT";
    public static final String DATA_JSON_WO_REGISTRATION = "DATA_WO_REGISTRATION";
    public static final String DATA_JSON = "DATA";

    public static final String TEI_JSON = "TEI";
    public static final String ENROLLMENT_JSON = "ENROLLMENT";
    public static final String EVENTS_JSON = "EVENTS";
    public static final String ATTR_JSON = "ATTR";
    public static final String RELATIONSHIP_JSON = "RELATIONSHIP";

    private String type;
    private String data;

    QRjson(String type, String data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public String getData() throws UnsupportedEncodingException {
        String info;
        byte[] decodedBytes = Base64.decode(data, Base64.DEFAULT);
        info = new String(decodedBytes, "UTF-8");

        return info;
    }
}
