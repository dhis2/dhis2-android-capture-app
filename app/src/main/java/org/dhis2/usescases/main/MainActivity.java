package org.dhis2.usescases.main;

import android.app.Activity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableInt;
import androidx.fragment.app.Fragment;

import com.andrognito.pinlockview.PinLockListener;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityMainBinding;
import org.dhis2.usescases.about.AboutFragment;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.jira.JiraFragment;
import org.dhis2.usescases.main.program.ProgramFragment;
import org.dhis2.usescases.qrReader.QrReaderFragment;
import org.dhis2.usescases.syncManager.ErrorDialog;
import org.dhis2.usescases.syncManager.SyncManagerFragment;
import org.dhis2.usescases.teiDashboard.nfc_data.NfcDataWriteActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;


public class MainActivity extends ActivityGlobalAbstract implements MainContracts.View {

    private static final int PERMISSION_REQUEST = 1987;
    public ActivityMainBinding binding;
    @Inject
    MainContracts.Presenter presenter;

    private ProgramFragment programFragment;

    ObservableInt currentFragment = new ObservableInt(R.id.menu_home);
    private boolean isPinLayoutVisible = false;

    private int fragId;
    private SharedPreferences prefs;
    private boolean backDropActive = false;
    private FiltersAdapter adapter;

    //-------------------------------------
    //region LIFECYCLE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Objects.requireNonNull(((App) getApplicationContext()).userComponent()).plus(new MainModule()).inject(this);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setPresenter(presenter);
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

        if (savedInstanceState != null) {
            int frag = savedInstanceState.getInt("Fragment");
            currentFragment.set(frag);
            binding.setCurrentFragment(currentFragment);
            changeFragment(frag);
        } else {
            binding.setCurrentFragment(currentFragment);
            changeFragment(R.id.menu_home);
        }

        prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);


        adapter = new FiltersAdapter();
        binding.filterLayout.setAdapter(adapter);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("Fragment", fragId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    PERMISSION_REQUEST);
        }

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
        Transition transition = new ChangeBounds();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition);
        backDropActive = !backDropActive;
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);
        if (backDropActive)
            initSet.connect(R.id.fragment_container, ConstraintSet.TOP, R.id.backdropGuide, ConstraintSet.BOTTOM, 0);
        else
            initSet.connect(R.id.fragment_container, ConstraintSet.TOP, R.id.toolbar, ConstraintSet.BOTTOM, 0);
        initSet.applyTo(binding.backdropLayout);
    }

    private void checkFilterEnabled() {
        binding.filter.setBackgroundColor(programFragment.areFiltersApplied() ? getAccentColor() : getPrimaryColor());
        binding.filter.setColorFilter(programFragment.areFiltersApplied() ? getPrimaryColor() : getAccentColor(), PorterDuff.Mode.SRC_IN);
        binding.filter.setBackgroundResource(programFragment.areFiltersApplied() ? R.drawable.white_circle : 0);
    }

    @Override
    public void onLockClick() {
        if (prefs.getString("pin", null) == null) {
            binding.drawerLayout.closeDrawers();
            binding.pinLayout.getRoot().setVisibility(View.VISIBLE);
            isPinLayoutVisible = true;
        } else
            presenter.blockSession(null);
    }

    @Override
    public void onBackPressed() {
        if (fragId != R.id.menu_home) {
            changeFragment(R.id.menu_home);
        } else if (isPinLayoutVisible) {
            isPinLayoutVisible = false;
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();
        } else
            super.onBackPressed();
    }

    @Override
    public void changeFragment(int id) {
        fragId = id;
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
            case R.id.nfc_scan:
                Intent intentNfc = new Intent(this, NfcDataWriteActivity.class);
                startActivity(intentNfc);
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

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, tag).commitAllowingStateLoss();
            binding.title.setText(tag);
        }
        binding.drawerLayout.closeDrawers();

    }

    @Override
    public void showSyncErrors(List<TrackerImportConflict> data) {
        new ErrorDialog().setData(data).show(getSupportFragmentManager().beginTransaction(), ErrorDialog.TAG);
    }

    @Override
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
    }

    public void setTitle(String title) {
        binding.title.setText(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showTutorial(boolean shaked) {
        if (fragId == R.id.menu_home || fragId == R.id.sync_manager)
            super.showTutorial(shaked);
        else
            showToast(getString(R.string.no_intructions));

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FilterManager.OU_TREE && resultCode == Activity.RESULT_OK) {
            adapter.notifyDataSetChanged();
            updateFilters(FilterManager.getInstance().getTotalFilters());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}