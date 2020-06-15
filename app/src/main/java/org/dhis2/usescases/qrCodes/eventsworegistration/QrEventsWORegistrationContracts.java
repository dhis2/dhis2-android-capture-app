package org.dhis2.usescases.qrCodes.eventsworegistration;

import androidx.annotation.NonNull;

import org.dhis2.usescases.qrCodes.QrViewModel;

import java.util.List;

public class QrEventsWORegistrationContracts {

    public interface View {
        void showQR(@NonNull List<QrViewModel> bitmaps);

        void onBackClick();

        void onPrevQr();

        void onNextQr();
    }

    public interface Presenter {
        void generateQrs(@NonNull String eventUid, @NonNull QrEventsWORegistrationContracts.View view);

        void onBackClick();

        void onPrevQr();

        void onNextQr();
    }
}
