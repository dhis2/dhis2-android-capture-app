package org.dhis2.usescases.qrCodes;

public class QrViewModel {

    private String qrType;

    private String qrJson;

    public QrViewModel(String qrType, String qrJson) {
        this.qrType = qrType;
        this.qrJson = qrJson;
    }

    public String getQrType() {
        return qrType;
    }

    public void setQrType(String qrType) {
        this.qrType = qrType;
    }

    public String getQrJson() {
        return qrJson;
    }

    public void setQrJson(String qrJson) {
        this.qrJson = qrJson;
    }
}
