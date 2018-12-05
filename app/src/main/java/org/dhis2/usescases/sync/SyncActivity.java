package org.dhis2.usescases.sync;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.dhis2.usescases.general.ActivityGlobalAbstract;

import javax.inject.Inject;

import androidx.work.State;

public class SyncActivity extends ActivityGlobalAbstract implements SyncContracts.View {
    @Inject
    SyncContracts.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void updateView(String data, State state) {

    }
}
