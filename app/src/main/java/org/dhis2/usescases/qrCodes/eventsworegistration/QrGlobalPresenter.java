package org.dhis2.usescases.qrCodes.eventsworegistration;

import org.dhis2.usescases.qrCodes.QrContracts;

public class QrGlobalPresenter {

    protected QrEventsWORegistrationContracts.View qrEventWORegistrationView;
    protected QrContracts.View qrView;

    public void onBackClick() {
        if (qrView != null)
            qrView.onBackClick();
        if (qrEventWORegistrationView != null)
            qrEventWORegistrationView.onBackClick();
    }

    public void onPrevQr() {
        if (qrView != null)
            qrView.onPrevQr();
        if (qrEventWORegistrationView != null)
            qrEventWORegistrationView.onPrevQr();
    }

    public void onNextQr() {
        if (qrView != null)
            qrView.onNextQr();
        if (qrEventWORegistrationView != null)
            qrEventWORegistrationView.onNextQr();
    }
}
