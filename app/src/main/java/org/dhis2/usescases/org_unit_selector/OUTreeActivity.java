package org.dhis2.usescases.org_unit_selector;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.OuTreeActivityBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.Iterator;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.ou_tree_activity);
        compositeDisposable = new CompositeDisposable();
        ouChildListener = PublishProcessor.create();

        D2 d2 = ((App) getApplicationContext()).serverComponent().userManager().getD2();

        compositeDisposable.add(
                d2.organisationUnitModule().organisationUnits
                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                        .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                        .byRootOrganisationUnit(true)
                        .getAsync()
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
                        .flatMap(parentOu ->
                                d2.organisationUnitModule().organisationUnits
                                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                                        .byParentUid().eq(parentOu.val1().uid())
                                        .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                                        .getAsync().toFlowable()
                                        .map(ouList -> Pair.create(parentOu.val0(), ouList)))
                        .filter(list -> binding.orgUnitRecycler.getAdapter() != null)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                organisationUnits ->
                                        ((OrgUnitSelectorAdapter) binding.orgUnitRecycler.getAdapter())
                                                .addOrgUnits(organisationUnits.val0(), organisationUnits.val1()),
                                Timber::e
                        )
        );

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
    public void onOrgUnitClick(OrganisationUnit organisationUnit, int position) {
        ouChildListener.onNext(Pair.create(position, organisationUnit));
    }
}
