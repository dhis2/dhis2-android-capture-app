package org.dhis2.usescases.qrCodes;

import android.support.annotation.NonNull;

import java.util.List;

public class QrContracts {

    public interface View {
        void showQR(@NonNull List<QrViewModel> bitmaps);

        void onBackClick();

        void onPrevQr();

        void onNextQr();
    }

    public interface Presenter {
        void generateQrs(@NonNull String teUid, @NonNull QrContracts.View view);

        void onBackClick();

        void onPrevQr();

        void onNextQr();
    }
}
