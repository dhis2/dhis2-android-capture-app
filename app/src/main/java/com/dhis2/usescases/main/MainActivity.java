package com.dhis2.usescases.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.andrognito.pinlockview.PinLockListener;
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
    private ProgramFragment programFragment;

    /*Lifecycle methods*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        AndroidInjection.inject(this);
        ((App) getApplicationContext()).getUserComponent().plus(new MainContractsModule()).inject(this);
        if (getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setPresenter(presenter);

        binding.pinLockView.attachIndicatorDots(binding.indicatorDots);
        binding.pinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                presenter.blockSession(pin);
            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {

            }
        });
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

    @Override
    public void showHideFilter() {

        programFragment.binding.filterLayout.setVisibility(programFragment.binding.filterLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLockClick(android.view.View view) {
        binding.drawerLayout.closeDrawers();
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        if (prefs.getString("pin", null) == null)
            binding.pinLayout.setVisibility(View.VISIBLE);
        else
            presenter.blockSession(null);

    }


    private void changeFragment() {
        programFragment = new ProgramFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, programFragment, "HOME").commit();
    }

    @Override
    public AndroidInjector<android.support.v4.app.Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

}