package com.dhis2.usescases.main.program;

import android.os.Bundle;

import com.dhis2.R;
import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailActivity;
import com.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import com.dhis2.utils.OrgUnitUtils;
import com.dhis2.utils.Period;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramPresenter implements ProgramContract.Presenter {

    private ProgramContract.View view;
    private final HomeRepository homeRepository;
    private final CompositeDisposable compositeDisposable;

    private List<OrganisationUnitModel> myOrgs;

    ProgramPresenter(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(ProgramContract.View view) {
        this.view = view;
        this.view = view;

        compositeDisposable.add(
                homeRepository.orgUnits()
                        .map(
                                orgUnits -> {
                                    this.myOrgs = orgUnits;
                                    return homeRepository.programs(orgUnitQuery());
                                }
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    view.swapProgramData().accept(data.blockingFirst());
                                    view.addTree(OrgUnitUtils.renderTree(view.getContext(), myOrgs));
                                },
                                throwable -> view.renderError(throwable.getMessage())));
    }


    @Override
    public void getProgramsWithDates(ArrayList<Date> dates, Period period) {
        compositeDisposable.add(homeRepository.programs(dates, period)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapProgramData(),
                        throwable -> view.renderError(throwable.getMessage())));

    }


    @Override
    public void getProgramsOrgUnit(List<Date> dates, Period period, String orgUnitQuery) {
        compositeDisposable.add(homeRepository.programs(dates, period, orgUnitQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapProgramData(),
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }


    @Override
    public void getAllPrograms(String orgUnitQuery) {
        compositeDisposable.add(
                homeRepository.programs(orgUnitQuery)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.swapProgramData(),
                                throwable -> view.renderError(throwable.getMessage())
                        ));
    }

    @Override
    public List<OrganisationUnitModel> getOrgUnits() {
        return myOrgs;
    }

    @Override
    public void onItemClick(ProgramModel programModel, Period currentPeriod) {

        Bundle bundle = new Bundle();
        bundle.putString("PROGRAM_UID", programModel.uid());
        bundle.putString("TRACKED_ENTITY_UID", programModel.trackedEntityType());

        switch (currentPeriod) {
            case NONE:
                bundle.putInt("CURRENT_PERIOD", R.string.period);
                bundle.putSerializable("CHOOSEN_DATE", null);
                break;
            case DAILY:
                bundle.putInt("CURRENT_PERIOD", R.string.DAILY);
                bundle.putSerializable("CHOOSEN_DATE", view.getChosenDateDay());
                break;
            case WEEKLY:
                bundle.putInt("CURRENT_PERIOD", R.string.WEEKLY);
                bundle.putSerializable("CHOOSEN_DATE", view.getChosenDateWeek());
                break;
            case MONTHLY:
                bundle.putInt("CURRENT_PERIOD", R.string.MONTHLY);
                bundle.putSerializable("CHOOSEN_DATE", view.getChosenDateMonth());
                break;
            case YEARLY:
                bundle.putInt("CURRENT_PERIOD", R.string.YEARLY);
                bundle.putSerializable("CHOOSEN_DATE", view.getChosenDateYear());
                break;
        }


        if (programModel.programType() == ProgramType.WITH_REGISTRATION) {
            view.startActivity(SearchTEActivity.class, bundle, false, false, null);
        } else {
            view.startActivity(ProgramEventDetailActivity.class, bundle, false, false, null);
        }
    }

    @Override
    public void onOrgUnitButtonClick() {
        view.openDrawer();
    }

    @Override
    public void onDateRangeButtonClick() {
        view.showRageDatePicker();
    }


    @Override
    public void onTimeButtonClick() {
        view.showTimeUnitPicker();
    }

    @Override
    public void showDescription(String description) {
        view.showDescription(description);
    }

    @Override
    public Observable<List<EventModel>> getEvents(ProgramModel programModel) {
        return homeRepository.eventModels(programModel.uid());
    }

    @Override
    public Observable<Pair<Integer, String>> getNumberOfRecords(ProgramModel programModel) {
        return homeRepository.numberOfRecords(programModel);
    }

    @Override
    public Flowable<State> syncState(ProgramModel program) {
        return homeRepository.syncState(program);
    }


    private String orgUnitQuery() {
        StringBuilder orgUnitFilter = new StringBuilder();
        for (int i = 0; i < myOrgs.size(); i++) {
            orgUnitFilter.append("'");
            orgUnitFilter.append(myOrgs.get(i).uid());
            orgUnitFilter.append("'");
            if (i < myOrgs.size() - 1)
                orgUnitFilter.append(", ");
        }
        view.setOrgUnitFilter(orgUnitFilter);
        return orgUnitFilter.toString();
    }
}
