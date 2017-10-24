package com.dhis2.usescases.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityMainBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.main.program.ProgramFragment;

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

    /*Lifecycle methods*/

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

    /*End of lifecycle methods*/

    /*User info methods*/

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

    /*End of user info methods*/

    @Override
    public void openDrawer(int gravity) {
        if (!binding.drawerLayout.isDrawerOpen(gravity))
            binding.drawerLayout.openDrawer(gravity);
        else
            binding.drawerLayout.closeDrawer(gravity);
    }


    private void changeFragment() {

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProgramFragment(), "HOME").commit();
    }

    @Override
    public AndroidInjector<android.support.v4.app.Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

}