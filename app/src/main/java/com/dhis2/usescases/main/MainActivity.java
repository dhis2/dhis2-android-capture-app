package com.dhis2.usescases.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableInt;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.andrognito.pinlockview.PinLockListener;
import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityMainBinding;
import com.dhis2.usescases.about.AboutFragment;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.jira.JiraFragment;
import com.dhis2.usescases.main.program.ProgramFragment;
import com.dhis2.usescases.qrReader.QrReaderFragment;
import com.dhis2.usescases.syncManager.SyncManagerFragment;
import com.dhis2.utils.Period;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;


public class MainActivity extends ActivityGlobalAbstract implements MainContracts.View {

    ActivityMainBinding binding;
    @Inject
    MainContracts.Presenter presenter;

    private ProgramFragment programFragment;

    ObservableInt currentFragment = new ObservableInt(R.id.menu_home);
    private boolean isPinLayoutVisible = false;

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
        binding.setCurrentFragment(currentFragment);
        binding.navView.setNavigationItemSelectedListener(item -> {
            changeFragment(item.getItemId());
            return false;
        });
        binding.pinLayout.pinLockView.attachIndicatorDots(binding.pinLayout.indicatorDots);
        binding.pinLayout.pinLockView.setPinLockListener(new PinLockListener() {
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

        changeFragment(R.id.menu_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);

    }

    @Override
    protected void onPause() {
        presenter.onDetach();
        super.onPause();
    }

    //endregion

    /*User info methods*/

    @NonNull
    @Override
    public Consumer<String> renderUsername() {
        return username -> {
            binding.setUserName(username);
            ((TextView) binding.navView.getHeaderView(0).findViewById(R.id.user_info)).setText(username);
//            binding.menuJira.setText(String.format(getString(R.string.jira_report) + " (%s)", BuildConfig.VERSION_NAME));
            binding.executePendingBindings();
        };
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
        checkFilterEnabled();
    }

    private void checkFilterEnabled() {
        int color = getPrimaryColor();
        if (programFragment.binding.filterLayout.getVisibility() == View.VISIBLE) {
            binding.filter.setBackgroundColor(color);
            binding.filter.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            binding.filter.setBackgroundResource(0);
        }
        // when filter layout is hidden
        else {
            // not applied period filter
            if (programFragment.getCurrentPeriod() == Period.NONE && programFragment.areAllOrgUnitsSelected()) {
                binding.filter.setBackgroundColor(color);
                binding.filter.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
                binding.filter.setBackgroundResource(0);
            }
            // applied period filter
            else {
                binding.filter.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
                binding.filter.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                binding.filter.setBackgroundResource(R.drawable.white_circle);
            }
        }
    }

    @Override
    public void onLockClick() {
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        if (prefs.getString("pin", null) == null) {
            binding.drawerLayout.closeDrawers();
            binding.pinLayout.getRoot().setVisibility(View.VISIBLE);
            isPinLayoutVisible = true;
        } else
            presenter.blockSession(null);
    }

    @Override
    public void onBackPressed() {
        if (isPinLayoutVisible) {
            isPinLayoutVisible = false;
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();
        } else
            super.onBackPressed();
    }

    @Override
    public void changeFragment(int id) {
        binding.navView.setCheckedItem(id);
        Fragment fragment = null;
        String tag = null;
        switch (id) {
            case R.id.sync_manager:
                fragment = new SyncManagerFragment();
                tag = getString(R.string.SYNC_MANAGER);
                binding.filter.setVisibility(View.GONE);
                break;
            case R.id.qr_scan:
                fragment = new QrReaderFragment();
                tag = getString(R.string.QR_SCANNER);
                binding.filter.setVisibility(View.GONE);
                break;
            case R.id.menu_jira:
                fragment = new JiraFragment();
                tag = getString(R.string.jira_report);
                binding.filter.setVisibility(View.GONE);
                break;
            case R.id.menu_about:
                fragment = new AboutFragment();
                tag = getString(R.string.about);
                binding.filter.setVisibility(View.GONE);
                break;
            case R.id.block_button:
                onLockClick();
                break;
            case R.id.logout_button:
                presenter.logOut();
                break;
            case R.id.menu_home:
            default:
                fragment = new ProgramFragment();
                programFragment = (ProgramFragment) fragment;
                tag = getString(R.string.done_task);
                binding.filter.setVisibility(View.VISIBLE);
                break;
        }

        if (fragment != null) {
            currentFragment.set(id);

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, tag).commit();
            binding.title.setText(tag);
        }
        binding.drawerLayout.closeDrawers();
    }

    public void setTitle(String title) {
        binding.title.setText(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}