package com.dhis2.usescases.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityMainBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.main.program.ProgramFragment;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.reactivex.functions.Consumer;


public class MainActivity extends ActivityGlobalAbstract implements MainContracts.View, HasSupportFragmentInjector {

    ActivityMainBinding binding;
    @Inject
    MainContracts.Presenter presenter;

    @Inject
    DispatchingAndroidInjector<android.support.v4.app.Fragment> dispatchingAndroidInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        AndroidInjection.inject(this);
        ((App) getApplicationContext()).getUserComponent().plus(new MainContractsModule()).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
        changeFragment();
    }

    @Override
    protected void onPause() {
        presenter.onDetach();
        super.onPause();
    }

    @NonNull
    @Override
    public Consumer<String> renderUsername() {
        return username -> {
            binding.setUserName(username);
            binding.executePendingBindings();
        };
    }

    @NonNull
    @Override
    public Consumer<String> renderUserInfo() {
        return (userInitials) -> Log.d("dhis", userInitials);
    }

    @NonNull
    @Override
    public Consumer<String> renderUserInitials() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void openDrawer(int gravity) {
        if (!binding.drawerLayout.isDrawerOpen(gravity))
            binding.drawerLayout.openDrawer(gravity);
        else
            binding.drawerLayout.closeDrawer(gravity);
    }

    @Override
    public Consumer<List<OrganisationUnitModel>> setOrgUnitTree() {
        return orgUnitList -> {
            Log.d("dhis_ORGUNIT", "Usuario tiene acceso a :" + orgUnitList.size());
            TreeNode root = TreeNode.root();
            for (OrganisationUnitModel orgUnit : orgUnitList) {
                if (orgUnit.level() == 1) {
                    TreeNode level_1 = new TreeNode(orgUnit.displayShortName());
                }
            }
        };
    }

    @NonNull
    @Override
    public Consumer<Payload<OrganisationUnit>> initOrgUnitTree() {
        return Payload::items;
    }


    private void changeFragment() {

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProgramFragment(), "HOME").commit();
    }

    @Override
    public AndroidInjector<android.support.v4.app.Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    public void addTree(TreeNode treeNode) {
        AndroidTreeView treeView = new AndroidTreeView(this, treeNode);
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        binding.treeViewContainer.addView(treeView.getView());
    }
}