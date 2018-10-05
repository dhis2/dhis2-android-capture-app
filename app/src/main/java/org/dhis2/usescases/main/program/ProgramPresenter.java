package org.dhis2.usescases.main.program;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity;
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity;
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.OrgUnitUtils;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramPresenter implements ProgramContract.Presenter {

    private ProgramContract.View view;
    private final HomeRepository homeRepository;
    private CompositeDisposable compositeDisposable;

    private List<OrganisationUnitModel> myOrgs;
    private FlowableProcessor<Trio> programQueries;

    ProgramPresenter(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    @Override
    public void init(ProgramContract.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        programQueries = PublishProcessor.create();
        compositeDisposable.add(
                homeRepository.orgUnits()
                        .subscribeOn(Schedulers.from(executorService))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    this.myOrgs = data;
                                    programQueries.onNext(Trio.create(null, null, null));
                                    view.addTree(OrgUnitUtils.renderTree(view.getContext(), myOrgs, true));
                                },
                                throwable -> view.renderError(throwable.getMessage())));

        compositeDisposable.add(
                programQueries
                        .flatMap(datePeriodOrgs -> homeRepository.programModels(
                                (List<Date>) datePeriodOrgs.val0(),
                                (Period) datePeriodOrgs.val1(),
                                (String) datePeriodOrgs.val2(),
                                myOrgs.size()))
                        .subscribeOn(Schedulers.from(executorService))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.swapProgramModelData(),
                                throwable -> view.renderError(throwable.getMessage())
                        ));
    }


    @Override
    public void getProgramsWithDates(ArrayList<Date> dates, Period period) {

        programQueries.onNext(Trio.create(dates, period, orgUnitQuery()));

    }


    @Override
    public void getProgramsOrgUnit(List<Date> dates, Period period, String orgUnitQuery) {
        programQueries.onNext(Trio.create(dates, period, orgUnitQuery));
    }


    @Override
    public void getAllPrograms(String orgUnitQuery) {
        programQueries.onNext(Trio.create(null, null, orgUnitQuery));
    }

    @Override
    public List<OrganisationUnitModel> getOrgUnits() {
        return myOrgs;
    }

    @Override
    public void dispose() {
        compositeDisposable.clear();
    }

    @Override
    public void onItemClick(ProgramViewModel programModel, Period currentPeriod) {

        Bundle bundle = new Bundle();
        String idTag;
        if (!isEmpty(programModel.type())) {
            bundle.putString("TRACKED_ENTITY_UID", programModel.type());
        }

        if (programModel.typeName().equals("DataSets"))
            idTag = "DATASET_UID";
        else
            idTag = "PROGRAM_UID";

        bundle.putString(idTag, programModel.id());

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

        int programTheme = ColorUtils.getThemeFromColor(programModel.color());
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (programTheme != -1) {
            prefs.edit().putInt(Constants.PROGRAM_THEME, programTheme).apply();
        } else
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();

        if (programModel.programType().equals(ProgramType.WITH_REGISTRATION.name())) {
            view.startActivity(SearchTEActivity.class, bundle, false, false, null);
        } else if (programModel.programType().equals(ProgramType.WITHOUT_REGISTRATION.name())) {
            view.startActivity(ProgramEventDetailActivity.class, bundle, false, false, null);
        } else {
            view.startActivity(DataSetDetailActivity.class, bundle, false, false, null);
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
    public Observable<Pair<Integer, String>> getNumberOfRecords(ProgramModel programModel) {
        return homeRepository.numberOfRecords(programModel);
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
