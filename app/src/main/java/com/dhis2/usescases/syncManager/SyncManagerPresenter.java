package com.dhis2.usescases.syncManager;

import android.content.Intent;

import com.dhis2.data.service.SyncService;

/**
 * Created by lmartin on 21/03/2018.
 */

public class SyncManagerPresenter implements SyncManagerContracts.Presenter{

    private SyncManagerContracts.View view;

    public SyncManagerPresenter(SyncManagerContracts.View view) {
        this.view = view;
    }

    @Override
    public void sync() {
        view.getContext().startService(new Intent(view.getContext().getApplicationContext(), SyncService.class));
    }
}
