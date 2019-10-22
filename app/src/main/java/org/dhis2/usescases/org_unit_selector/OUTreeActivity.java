package org.dhis2.usescases.org_unit_selector;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.OuTreeActivityBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class OUTreeActivity extends ActivityGlobalAbstract implements OrgUnitSelectorAdapter.OnOrgUnitClick {
    OuTreeActivityBinding binding;
    CompositeDisposable compositeDisposable;
    FlowableProcessor<Pair<Integer, OrganisationUnit>> ouChildListener;
    FlowableProcessor<String> onSearchListener;
    FlowableProcessor<Boolean> onStartSearch;

    public static Bundle getBundle(
            @Nullable String programUid
    ) {
        Bundle bundle = new Bundle();

        if (programUid != null)
            bundle.putString("PROGRAM", programUid);
        return bundle;
    }

    @SuppressLint("RxDefaultScheduler")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.ou_tree_activity);
        compositeDisposable = new CompositeDisposable();
        ouChildListener = PublishProcessor.create();
        onSearchListener = PublishProcessor.create();
        onStartSearch = PublishProcessor.create();

        String programUid = getIntent().getStringExtra("PROGRAM");

        D2 d2 = ((App) getApplicationContext()).serverComponent().userManager().getD2();

        compositeDisposable.add(
                onStartSearch
                        .flatMap(e -> d2.organisationUnitModule().organisationUnits()
                                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                                .byRootOrganisationUnit(true)
                                .get().toFlowable()
                                .map(list -> {
                                    int minLevel = list.get(0).level();
                                    for (OrganisationUnit ou : list)
                                        minLevel = ou.level() < minLevel ? ou.level() : minLevel;
                                    Iterator<OrganisationUnit> it = list.iterator();
                                    while (it.hasNext()) {
                                        if (it.next().level() > minLevel)
                                            it.remove();
                                    }
                                    return list;
                                })
                                .map(organisationUnits -> {
                                    List<TreeNode> nodes = new ArrayList<>();
                                    for (OrganisationUnit org : organisationUnits) {
                                        nodes.add(new TreeNode(org,
                                                false,
                                                !d2.organisationUnitModule().organisationUnits().byParentUid().eq(org.uid()).blockingIsEmpty(),
                                                FilterManager.getInstance().getOrgUnitFilters().contains(org),
                                                org.level()));
                                    }
                                    return nodes;
                                }))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                organisationUnits ->
                                        binding.orgUnitRecycler.setAdapter(
                                                new OrgUnitSelectorAdapter(organisationUnits, this)),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                ouChildListener
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .flatMap(parentOu ->
                                d2.organisationUnitModule().organisationUnits()
                                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                                        .byParentUid().eq(parentOu.val1().uid())
                                        .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                                        .get().toFlowable()
                                        .map(ouList -> Pair.create(parentOu.val0(), ouList)))
                        .filter(list -> binding.orgUnitRecycler.getAdapter() != null)
                        .map(organisationUnits -> {
                            List<TreeNode> nodes = new ArrayList<>();
                            for (OrganisationUnit org : organisationUnits.val1()) {
                                nodes.add(new TreeNode(org,
                                        false,
                                        !d2.organisationUnitModule().organisationUnits().byParentUid().eq(org.uid()).blockingIsEmpty(),
                                        FilterManager.getInstance().getOrgUnitFilters().contains(org),
                                        org.level()));
                            }
                            return Pair.create(organisationUnits.val0(), nodes);
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                organisationUnits ->
                                        ((OrgUnitSelectorAdapter) binding.orgUnitRecycler.getAdapter())
                                                .addOrgUnits(organisationUnits.val0(), organisationUnits.val1()),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                onSearchListener
                        .filter(name ->
                                name.length() > 3
                        )
                        .flatMap(name -> d2.organisationUnitModule().organisationUnits()
                                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                                .byDisplayName().like("%" + name + "%")
                                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                                .get().toFlowable())
                        .filter(name -> binding.orgUnitRecycler.getAdapter() != null)
                        .map(organisationUnits -> {
                            List<TreeNode> nodes = new ArrayList<>();
                            List<String> orderedList = new ArrayList<>();
                            for (OrganisationUnit org : organisationUnits) {
                                for (String str : org.path().split("/")) {
                                    orderedList = addToArray(orderedList, str);
                                }
                                orderedList = addToArray(orderedList, org.uid());
                            }
                            for (String str : orderedList) {
                                OrganisationUnit organisationUnitParent = d2.organisationUnitModule().organisationUnits()
                                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                                        .byUid().eq(str).one().blockingGet();
                                if (organisationUnitParent != null)
                                    nodes.add(
                                            new TreeNode(organisationUnitParent,
                                                    false,
                                                    !d2.organisationUnitModule().organisationUnits().byParentUid().eq(organisationUnitParent.uid()).blockingIsEmpty(),
                                                    FilterManager.getInstance().getOrgUnitFilters().contains(organisationUnitParent),
                                                    organisationUnitParent.level())
                                    );
                            }
                            for (int i = 1; i < nodes.size(); i++) {
                                if (nodes.get(i).getLevel() > nodes.get(i - 1).getLevel()) {
                                    nodes.get(i - 1).setOpen(true);
                                }
                            }
                            return nodes;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                organisationUnits ->
                                        binding.orgUnitRecycler.setAdapter(
                                                new OrgUnitSelectorAdapter(organisationUnits, this)),
                                Timber::e
                        )
        );
        binding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 0) {
                    onStartSearch.onNext(true);
                } else {
                    onSearchListener.onNext(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Not used
            }
        });
        onStartSearch.onNext(true);
        binding.clearAll.setOnClickListener(v -> {
            if ((binding.orgUnitRecycler.getAdapter()) != null) {
                ((OrgUnitSelectorAdapter) binding.orgUnitRecycler.getAdapter()).clearAll();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    public void onBackClick(View view) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onOrgUnitClick(TreeNode node, int position) {
        ouChildListener.onNext(Pair.create(position, node.getContent()));
    }

    public List<String> addToArray(List<String> list, String uuid) {
        if (!list.contains(uuid))
            list.add(uuid);
        return list;
    }
}
