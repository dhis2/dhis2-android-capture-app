package com.dhis2.usescases.qrCodes;

import android.graphics.Bitmap;

public class QrViewModel {

    private String qrType;

    private Bitmap qrBitmap;

    public QrViewModel(String qrType, Bitmap qrBitmap) {
        this.qrType = qrType;
        this.qrBitmap = qrBitmap;
    }

    public String getQrType() {
        return qrType;
    }

    public void setQrType(String qrType) {
        this.qrType = qrType;
    }

    public Bitmap getQrBitmap() {
        return qrBitmap;
    }

    public void setQrBitmap(Bitmap qrBitmap) {
        this.qrBitmap = qrBitmap;
    }
}
