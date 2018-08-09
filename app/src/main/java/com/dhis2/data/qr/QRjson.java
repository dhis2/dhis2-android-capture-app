package com.dhis2.data.qr;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */

public class QRjson {

    public static final String EVENT_JSON = "EVENT";
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

    public String getData() {
        return data;
    }
}
