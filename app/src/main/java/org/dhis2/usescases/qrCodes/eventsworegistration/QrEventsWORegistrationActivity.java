package org.dhis2.usescases.qrCodes.eventsworegistration;

import android.os.Bundle;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.usescases.qrCodes.QrAdapter;
import org.dhis2.usescases.qrCodes.QrGlobalActivity;

import java.util.ArrayList;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

/**
 * QUADRAM. Created by ppajuelo on 21/06/2018.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class QrEventsWORegistrationActivity extends QrGlobalActivity implements QrEventsWORegistrationContracts.View {

    @Inject
    public QrEventsWORegistrationContracts.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new QrEventsWORegistrationModule()).inject(this);
        super.onCreate(savedInstanceState);
        activityQrEventsWoRegistrationCodesBinding = DataBindingUtil.setContentView(this, R.layout.activity_qr_events_wo_registration_codes);
        activityQrEventsWoRegistrationCodesBinding.setName(getString(R.string.share_qr));
        activityQrEventsWoRegistrationCodesBinding.setPresenter(presenter);
        String eventUid = getIntent().getStringExtra("EVENT_UID");

        qrAdapter = new QrAdapter(getSupportFragmentManager(), new ArrayList<>());
        activityQrEventsWoRegistrationCodesBinding.viewPager.setAdapter(qrAdapter);

        presenter.generateQrs(eventUid, this);
    }
}
