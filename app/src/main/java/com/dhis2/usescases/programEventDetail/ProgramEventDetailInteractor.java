package com.dhis2.usescases.programEventDetail;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.main.program.OrgUnitHolder;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class ProgramEventDetailInteractor implements ProgramEventDetailContract.Interactor {

    private final MetadataRepository metadataRepository;
    private final ProgramEventDetailRepository programEventDetailRepository;
    private ProgramEventDetailContract.View view;
    private String programId;
    private CompositeDisposable compositeDisposable;
    private CategoryOptionComboModel categoryOptionComboModel;

    private Date fromDate;
    private Date toDate;

    private List<Date> dates;
    private Period period;

    private @LastSearchType int lastSearchType;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LastSearchType.DATES, LastSearchType.DATE_RANGES})
    public @interface LastSearchType {
        int DATES = 1;
        int DATE_RANGES = 32;
    }

    ProgramEventDetailInteractor(ProgramEventDetailRepository programEventDetailRepository, MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.programEventDetailRepository = programEventDetailRepository;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(ProgramEventDetailContract.View view, String programId) {
        this.view = view;
        this.programId = programId;
        getProgram();
        getOrgUnits();
        getEvents(programId, DateUtils.getInstance().getToday(), DateUtils.getInstance().getToday());
    }

    @Override
    public void getEvents(String programId, Date fromDate, Date toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        lastSearchType = LastSearchType.DATES;
        Observable.just(programEventDetailRepository.filteredProgramEvents(programId,
                DateUtils.getInstance().formatDate(fromDate),
                DateUtils.getInstance().formatDate(toDate),
                categoryOptionComboModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setData,
                        Timber::e));
    }

    @Override
    public void getOrgUnits() {
        compositeDisposable.add(programEventDetailRepository.orgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::renderTree,
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }

    @Override
    public void getProgramEventsWithDates(String programId, List<Date> dates, Period period) {
        this.dates = dates;
        this.period = period;
        lastSearchType = LastSearchType.DATE_RANGES;
        compositeDisposable.add(programEventDetailRepository.filteredProgramEvents(programId, dates, period, categoryOptionComboModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setData,
                        throwable -> view.renderError(throwable.getMessage())));
    }

    @Override
    public void updateFilters(CategoryOptionComboModel categoryOptionComboModel) {
        this.categoryOptionComboModel = categoryOptionComboModel;
        switch (lastSearchType){
            case LastSearchType.DATES:
                getEvents(programId, this.fromDate, this.toDate);
                break;
            case LastSearchType.DATE_RANGES:
                getProgramEventsWithDates(programId, this.dates, this.period);
                break;
            default:
                getEvents(programId, DateUtils.getInstance().getToday(), DateUtils.getInstance().getToday());
                break;
        }
    }


    private void getProgram() {
        compositeDisposable.add(metadataRepository.getProgramWithId(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (programModel) -> {
                            view.setProgram(programModel);
                            getCatCombo(programModel);
                        },
                        Timber::d)
        );
    }

    private void getCatCombo(ProgramModel programModel){
        compositeDisposable.add(metadataRepository.getCategoryComboWithId(programModel.categoryCombo())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (catCombo) -> compositeDisposable.add(programEventDetailRepository.catCombo(programModel.categoryCombo())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        (catComboOptions) -> view.setCatComboOptions(catCombo, catComboOptions),
                                        Timber::d)
                        ),
                        Timber::d)
        );
    }


    private void renderTree(@NonNull List<OrganisationUnitModel> myOrgs) {

        HashMap<Integer, ArrayList<TreeNode>> subLists = new HashMap<>();

        List<OrganisationUnitModel> allOrgs = new ArrayList<>();
        allOrgs.addAll(myOrgs);
        for (OrganisationUnitModel myorg : myOrgs) {
            String[] path = myorg.path().split("/");
            for (int i = myorg.level() - 1; i > 0; i--) {
                OrganisationUnitModel orgToAdd = OrganisationUnitModel.builder()
                        .uid(path[i])
                        .level(i)
                        .parent(path[i - 1])
                        .name(path[i])
                        .displayName(path[i])
                        .displayShortName(path[i])
                        .build();
                if (!allOrgs.contains(orgToAdd))
                    allOrgs.add(orgToAdd);
            }
        }

        Collections.sort(myOrgs, (org1, org2) -> org2.level().compareTo(org1.level()));

        for (int i = 0; i < myOrgs.get(0).level(); i++) {
            subLists.put(i + 1, new ArrayList<>());
        }

        //Separamos las orunits en listas por nivel
        for (OrganisationUnitModel orgs : allOrgs) {
            ArrayList<TreeNode> sublist = subLists.get(orgs.level());
            TreeNode treeNode = new TreeNode(orgs).setViewHolder(new OrgUnitHolder(view.getContext()));
            treeNode.setSelectable(orgs.path() != null);
            sublist.add(treeNode);
            subLists.put(orgs.level(), sublist);
        }

        TreeNode root = TreeNode.root();
        root.addChildren(subLists.get(1));

        for (int level = myOrgs.get(0).level(); level > 1; level--) {
            for (TreeNode treeNode : subLists.get(level - 1)) {
                for (TreeNode treeNodeLevel : subLists.get(level)) {
                    if (((OrganisationUnitModel) treeNodeLevel.getValue()).parent().equals(((OrganisationUnitModel) treeNode.getValue()).uid()))
                        treeNode.addChild(treeNodeLevel);
                }
            }
        }

        view.addTree(root);

    }

    @Override
    public void onDettach() {
        compositeDisposable.dispose();
    }
}
