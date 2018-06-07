package com.dhis2.usescases.main.program;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageView;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailActivity;
import com.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import com.dhis2.utils.OrgUnitUtils;
import com.dhis2.utils.Period;
import com.dhis2.utils.StringUtils;

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
import timber.log.Timber;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramPresenter implements ProgramContract.Presenter {

    private final MetadataRepository metadataRepository;
    private ProgramContract.View view;
    private final HomeRepository homeRepository;
    private CompositeDisposable compositeDisposable;

    private List<OrganisationUnitModel> myOrgs;

    ProgramPresenter(HomeRepository homeRepository, MetadataRepository metadataRepository) {
        this.homeRepository = homeRepository;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(ProgramContract.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();

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
    public void programObjectStyle(ImageView programImageView, ProgramModel programModel) {
        compositeDisposable.add(
                metadataRepository.getObjectStyle(programModel.uid())
                        .filter(objectStyleModel -> objectStyleModel != null)
                        .map(objectStyleModel -> {
                            String color = objectStyleModel.color();
                            if (color != null && color.length() == 4) {//Color is formatted as #fff
                                char r = color.charAt(1);
                                char g = color.charAt(2);
                                char b = color.charAt(3);
                                color = "#" + r + r + g + g + b + b; //formatted to #ffff
                            }

                            int icon = -1;
                            if (objectStyleModel.icon() != null) {
                                Resources resources = view.getContext().getResources();
                                String iconName = objectStyleModel.icon().startsWith("ic_") ? objectStyleModel.icon() : "ic_" + objectStyleModel.icon();
                                icon = resources.getIdentifier(iconName, "drawable", view.getContext().getPackageName());
                            }
                            return Pair.create(color!=null?Color.parseColor(color):-1, icon);

                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                colorAndIcon -> {

                                    if (colorAndIcon.val1() != -1) {
                                        programImageView.setImageResource(colorAndIcon.val1());
                                    }

                                    if (colorAndIcon.val0() != -1) {
                                        programImageView.setBackgroundColor(colorAndIcon.val0());
                                        StringUtils.setFromResBgColor(programImageView, colorAndIcon.val0());

                                    } else {
                                        TypedValue typedValue = new TypedValue();
                                        TypedArray a = view.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryLight});
                                        int lcolor = a.getColor(0, 0);
                                        a.recycle();
                                        programImageView.setBackgroundColor(lcolor);
                                    }

                                },
                                Timber::d)
        );
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
