package com.dhis2.usescases.eventInitial;

import android.app.DatePickerDialog;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.program.ProgramModel;

import timber.log.Timber;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInitialPresenter implements EventInitialContract.Presenter {

    static private EventInitialContract.View view;
    private final EventInitialContract.Interactor interactor;
    public ProgramModel program;

    EventInitialPresenter(EventInitialContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(EventInitialContract.View mview, String programId, String eventId) {
        view = mview;
        interactor.init(view, programId, eventId);
    }

    @Override
    public void setProgram(ProgramModel program) {
        this.program = program;
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void createEvent() {
        // TODO CRIS
    }

    @Override
    public void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener) {
        view.showDateDialog(listener);
    }

    @Override
    public void onOrgUnitButtonClick() {
        view.openDrawer();
    }

    @Override
    public void onLocationClick() {
        Timber.d("MENSAJE", "clickado");
    }

    @Override
    public void onLocation2Click() {
        Timber.d("MENSAJE", "clickado2");
    }

    @Override
    public void getCatOption(String categoryOptionComboId) {
        interactor.getCatOption(categoryOptionComboId);
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }
}
