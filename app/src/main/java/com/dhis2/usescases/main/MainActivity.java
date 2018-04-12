package com.dhis2.usescases.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.andrognito.pinlockview.PinLockListener;
import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityMainBinding;
import com.dhis2.usescases.appInfo.AppInfoFragment;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.main.program.ProgramFragment;
import com.dhis2.usescases.syncManager.SyncManagerFragment;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;


public class MainActivity extends ActivityGlobalAbstract implements MainContracts.View {

    ActivityMainBinding binding;
    @Inject
    MainContracts.Presenter presenter;

    private ProgramFragment programFragment;

    //-------------------------------------
    //region LIFECYCLE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new MainModule()).inject(this);
        if (getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setPresenter(presenter);
        binding.filter.setOnLongClickListener(view -> {
            presenter.sync();
            return true;
        });
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

        presenter.init(this);
        changeFragment(R.id.menu_done_tasks);
    }

    @Override
    protected void onStop() {
        presenter.onDetach();
        super.onStop();
    }

    //endregion

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

    @Override
    public void changeFragment(int id) {
        Fragment fragment = null;
        String tag = null;
        if (id == R.id.menu_done_tasks) {
            fragment = new ProgramFragment();
            programFragment = (ProgramFragment) fragment;
            tag = "Done Task";
            binding.filter.setVisibility(View.VISIBLE);
        } else {
            /*fragment = new AppInfoFragment();
            tag = "Info";*/
            fragment = new SyncManagerFragment();
            tag = "SYNC Manager";

            binding.filter.setVisibility(View.GONE);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, tag).commit();
        binding.title.setText(tag);
        binding.drawerLayout.closeDrawers();
    }

}