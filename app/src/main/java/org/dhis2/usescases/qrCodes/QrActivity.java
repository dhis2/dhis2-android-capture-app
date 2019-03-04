package org.dhis2.usescases.qrCodes;

import android.os.Bundle;

import org.dhis2.App;
import org.dhis2.R;

import java.util.ArrayList;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

/**
 * QUADRAM. Created by ppajuelo on 21/06/2018.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class QrActivity extends QrGlobalActivity implements QrContracts.View {

    @Inject
    public QrContracts.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new QrModule()).inject(this);
        super.onCreate(savedInstanceState);
        activityQrCodesBinding = DataBindingUtil.setContentView(this, R.layout.activity_qr_codes);
        activityQrCodesBinding.setName(getString(R.string.share_qr));
        activityQrCodesBinding.setPresenter(presenter);
        String teiUid = getIntent().getStringExtra("TEI_UID");

        qrAdapter = new QrAdapter(getSupportFragmentManager(), new ArrayList<>());
        activityQrCodesBinding.viewPager.setAdapter(qrAdapter);
        presenter.generateQrs(teiUid, this);
    }
}
