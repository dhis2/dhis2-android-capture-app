package org.dhis2.usescases.datasets.dataSetTable;

import static org.dhis2.commons.extensions.ViewExtensionsKt.closeKeyboard;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.jakewharton.rxbinding2.view.RxView;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.Constants;
import org.dhis2.commons.dialogs.AlertBottomDialog;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.databinding.ActivityDatasetTableBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetDetail.DataSetDetailFragment;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSection;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionFragment;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.commons.sync.SyncContext;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.dhis2.utils.granularsync.SyncStatusDialogNavigatorKt;
import org.dhis2.utils.validationrules.ValidationResultViolationsAdapter;
import org.dhis2.utils.validationrules.Violation;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import kotlin.Unit;

public class DataSetTableActivity extends ActivityGlobalAbstract implements DataSetTableContract.View {

    String orgUnitUid;
    String catOptCombo;
    String dataSetUid;
    String periodId;

    boolean accessDataWrite;

    @Inject
    DataSetTableContract.Presenter presenter;

    private ActivityDatasetTableBinding binding;
    private boolean backPressed;
    private DataSetTableComponent dataSetTableComponent;

    private BottomSheetBehavior<View> behavior;
    private FlowableProcessor<Boolean> reopenProcessor;
    private boolean isKeyboardOpened = false;

    private static final String DATAVALUE_SYNC = "DATAVALUE_SYNC";

    public static Bundle getBundle(@NonNull String dataSetUid,
                                   @NonNull String orgUnitUid,
                                   @NonNull String periodId,
                                   @NonNull String catOptCombo) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        bundle.putString(Constants.ORG_UNIT, orgUnitUid);
        bundle.putString(Constants.PERIOD_ID, periodId);
        bundle.putString(Constants.CAT_COMB, catOptCombo);
        return bundle;
    }

    public static Intent intent(
            @NonNull Context context,
            @NonNull String dataSetUid,
            @NonNull String orgUnitUid,
            @NonNull String periodId,
            @NonNull String catOptCombo) {
        Intent intent = new Intent(context, DataSetTableActivity.class);
        intent.putExtras(getBundle(dataSetUid, orgUnitUid, periodId, catOptCombo));
        return intent;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        orgUnitUid = getIntent().getStringExtra(Constants.ORG_UNIT);
        periodId = getIntent().getStringExtra(Constants.PERIOD_ID);
        catOptCombo = getIntent().getStringExtra(Constants.CAT_COMB);
        dataSetUid = getIntent().getStringExtra(Constants.DATA_SET_UID);
        accessDataWrite = getIntent().getBooleanExtra(Constants.ACCESS_DATA, true);
        reopenProcessor = PublishProcessor.create();

        dataSetTableComponent = ((App) getApplicationContext()).userComponent()
                .plus(new DataSetTableModule(this,
                        dataSetUid,
                        periodId,
                        orgUnitUid,
                        catOptCombo
                ));
        dataSetTableComponent.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_table);
        binding.setPresenter(presenter);
        ViewExtensionsKt.clipWithRoundedCorners(binding.container, ExtensionsKt.getDp(16));
        binding.BSLayout.bottomSheetLayout.setVisibility(View.GONE);
        presenter.init(orgUnitUid, catOptCombo, periodId);
        binding.navigationView.selectItemAt(1);
        binding.navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_details:
                    binding.syncButton.setVisibility(View.VISIBLE);
                    binding.tabLayout.setVisibility(View.GONE);
                    openDetails();
                    break;
                case R.id.navigation_data_entry:
                    binding.syncButton.setVisibility(View.GONE);
                    binding.tabLayout.setVisibility(View.VISIBLE);
                    openSection(presenter.getFirstSection());
                    break;
            }
            return true;
        });
        openSection(presenter.getFirstSection());
        binding.syncButton.setOnClickListener(view -> showGranularSync());

        if (SyncStatusDialogNavigatorKt.shouldLaunchSyncDialog(getIntent())) {
            showGranularSync();
        }
    }

    private void openDetails() {
        DataSetDetailFragment fragment = DataSetDetailFragment.create(dataSetUid, accessDataWrite);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commitAllowingStateLoss();
    }

    private void openSection(String sectionUid) {
        DataSetSectionFragment fragment = DataSetSectionFragment.create(
                sectionUid,
                accessDataWrite,
                dataSetUid,
                orgUnitUid,
                periodId,
                catOptCombo);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commitAllowingStateLoss();
    }

    private void showGranularSync() {
        presenter.onClickSyncStatus();
        new SyncStatusDialog.Builder()
                .withContext(this)
                .withSyncContext(
                        new SyncContext.DataSetInstance(
                                dataSetUid,
                                periodId,
                                orgUnitUid,
                                catOptCombo
                        )
                )
                .onDismissListener(hasChanged -> {
                    if (hasChanged) presenter.updateData();
                }).show(DATAVALUE_SYNC);
    }

    @Override
    public void startInputEdition() {
        isKeyboardOpened = true;
        binding.navigationView.setVisibility(View.GONE);
        binding.saveButton.hide();
        if (binding.BSLayout.bottomSheetLayout.getVisibility() == View.VISIBLE) {
            if (behavior != null && behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            closeBottomSheet();
        }
    }

    @Override
    public void finishInputEdition() {
        isKeyboardOpened = false;
        binding.navigationView.setVisibility(View.VISIBLE);
        binding.saveButton.show();
        if (behavior != null) {
            showBottomSheet();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDettach();
    }

    @Override
    public void setSections(List<DataSetSection> sections) {
        for (DataSetSection section : sections) {
            TabLayout.Tab tab = binding.tabLayout.newTab();
            tab.setText(section.title());
            tab.setTag(section.getUid());
            binding.tabLayout.addTab(tab);
        }
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    closeKeyboard(currentFocus);
                }
                openSection((String) tab.getTag());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //unused
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //unused
            }
        });
    }

    public DataSetTableContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public Boolean accessDataWrite() {
        return accessDataWrite;
    }

    @Override
    public String getDataSetUid() {
        return dataSetUid;
    }

    @Override
    public void renderDetails(DataSetRenderDetails dataSetRenderDetails) {
        binding.dataSetName.setText(dataSetRenderDetails.title());
        binding.dataSetSubtitle.setText(dataSetRenderDetails.subtitle());
    }

    public void update() {
        presenter.init(orgUnitUid, catOptCombo, periodId);
    }

    @Override
    public void back() {
        if (getCurrentFocus() == null || backPressed)
            super.back();
        else {
            backPressed = true;
            binding.getRoot().requestFocus();
            back();
        }
    }

    public boolean isBackPressed() {
        return backPressed;
    }

    public DataSetTableComponent getDataSetTableComponent() {
        return dataSetTableComponent;
    }

    @Override
    public Observable<Object> observeSaveButtonClicks() {
        return RxView.clicks(binding.saveButton).doOnNext(o -> {
            if (getCurrentFocus() != null) {
                View currentFocus = getCurrentFocus();
                currentFocus.clearFocus();
                closeKeyboard(currentFocus);
            }
        });
    }

    @Override
    public void showMandatoryMessage(boolean isMandatoryFields) {
        String message;
        if (isMandatoryFields) {
            message = getString(R.string.field_mandatory_v2);
        } else {
            message = getString(R.string.field_required);
        }
        AlertBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.saved))
                .setMessage(message)
                .setPositiveButton(getString(R.string.button_ok), () -> Unit.INSTANCE)
                .show(getSupportFragmentManager(), AlertBottomDialog.class.getSimpleName());
    }

    @Override
    public void showValidationRuleDialog() {
        AlertBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.saved))
                .setMessage(getString(R.string.run_validation_rules))
                .setPositiveButton(getString(R.string.yes), () -> {
                    presenter.executeValidationRules();
                    return Unit.INSTANCE;
                })
                .setNegativeButton(getString(R.string.no), () -> {
                    if (presenter.isComplete()) {
                        finish();
                    } else {
                        showSuccessValidationDialog();
                    }
                    return Unit.INSTANCE;
                })
                .show(getSupportFragmentManager(), AlertBottomDialog.class.getSimpleName());
    }

    @Override
    public void showSuccessValidationDialog() {
        AlertBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.validation_success_title))
                .setMessage(getString(R.string.mark_dataset_complete))
                .setPositiveButton(getString(R.string.yes), () -> {
                    presenter.completeDataSet();
                    return Unit.INSTANCE;
                })
                .setNegativeButton(getString(R.string.no), () -> {
                    finish();
                    return Unit.INSTANCE;
                })
                .show(getSupportFragmentManager(), AlertBottomDialog.class.getSimpleName());
    }

    @Override
    public void savedAndCompleteMessage() {
        Toast.makeText(this, R.string.dataset_saved_completed, Toast.LENGTH_SHORT).show();
        finish();
    }


    @Override
    public void showErrorsValidationDialog(List<Violation> violations) {
        configureShapeDrawable();
        binding.BSLayout.dotsIndicator.setVisibility(violations.size() > 1 ? View.VISIBLE : View.INVISIBLE);
        initValidationErrorsDialog();
        binding.BSLayout.setErrorCount(violations.size());
        binding.BSLayout.title.setText(
                getResources().getQuantityText(R.plurals.error_message, violations.size())
        );
        binding.BSLayout.violationsViewPager.setAdapter(new ValidationResultViolationsAdapter(this, violations));
        binding.BSLayout.dotsIndicator.setViewPager(binding.BSLayout.violationsViewPager);

        behavior = BottomSheetBehavior.from(binding.BSLayout.bottomSheetLayout);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        animateArrowDown();
                        binding.saveButton.animate()
                                .translationY(0)
                                .start();
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        animateArrowUp();
                        binding.saveButton.animate()
                                .translationY(-ExtensionsKt.getDp(48))
                                .start();
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_SETTLING:
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                /*UnUsed*/
            }

            private void animateArrowDown() {
                binding.BSLayout.collapseExpand.animate()
                        .scaleY(-1f).setDuration(200)
                        .start();
            }

            private void animateArrowUp() {
                binding.BSLayout.collapseExpand.animate()
                        .scaleY(1f).setDuration(200)
                        .start();
            }
        });
    }

    @Override
    public void showCompleteToast() {
        Snackbar.make(binding.content, R.string.dataset_completed, BaseTransientBottomBar.LENGTH_SHORT)
                .show();
        finish();
    }

    @Override
    public void collapseExpandBottom() {
        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            if (isKeyboardOpened) {
                hideKeyboard();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }, 100);
            } else {
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public void closeBottomSheet() {
        binding.BSLayout.bottomSheetLayout.setVisibility(View.GONE);
    }

    private void showBottomSheet() {
        binding.BSLayout.bottomSheetLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void completeBottomSheet() {
        closeBottomSheet();
        presenter.completeDataSet();
    }

    @Override
    public boolean isErrorBottomSheetShowing() {
        return binding.BSLayout.bottomSheetLayout.getVisibility() == View.VISIBLE;
    }

    private void configureShapeDrawable() {
        int cornerSize = getResources().getDimensionPixelSize(R.dimen.rounded_16);
        ShapeAppearanceModel appearanceModel = new ShapeAppearanceModel().toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, cornerSize)
                .setTopRightCorner(CornerFamily.ROUNDED, cornerSize)
                .build();

        int elevation = getResources().getDimensionPixelSize(R.dimen.elevation);
        MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(appearanceModel);
        int color = ResourcesCompat.getColor(getResources(), R.color.white, null);
        shapeDrawable.setFillColor(ColorStateList.valueOf(color));

        binding.BSLayout.bottomSheetLayout.setBackground(shapeDrawable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.BSLayout.bottomSheetLayout.setElevation(elevation);
        } else {
            ViewCompat.setElevation(binding.BSLayout.bottomSheetLayout, elevation);
        }
    }

    private void initValidationErrorsDialog() {
        binding.BSLayout.bottomSheetLayout.setTranslationY(ExtensionsKt.getDp(48));
        binding.BSLayout.bottomSheetLayout.setVisibility(View.VISIBLE);
        binding.BSLayout.bottomSheetLayout.animate()
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .translationY(0)
                .start();
        binding.saveButton.animate()
                .translationY(-ExtensionsKt.getDp(48))
                .start();
    }


    public void showMoreOptions(View view) {
        new AppMenuHelper.Builder()
                .menu(this, R.menu.dataset_menu)
                .anchor(view)
                .onMenuInflated(popupMenu -> {
                    popupMenu.getMenu().findItem(R.id.reopen).setVisible(presenter.isComplete());
                    return Unit.INSTANCE;
                })
                .onMenuItemClicked(itemId -> {
                    if (itemId == R.id.showHelp) {
                        analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                        showTutorial(true);
                    } else if (itemId == R.id.reopen) {
                        showReopenDialog();
                    }
                    return true;
                })
                .build()
                .show();
    }

    private void showReopenDialog() {
        AlertBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.are_you_sure))
                .setMessage(getString(R.string.reopen_question))
                .setPositiveButton(getString(R.string.yes), () -> {
                    presenter.reopenDataSet();
                    return Unit.INSTANCE;
                })
                .setNegativeButton(getString(R.string.no), () -> Unit.INSTANCE)
                .show(getSupportFragmentManager(), AlertBottomDialog.class.getSimpleName());
    }

    @Override
    public void displayReopenedMessage(boolean done) {
        if (done) {
            Toast.makeText(this, R.string.action_done, Toast.LENGTH_SHORT).show();
            reopenProcessor.onNext(true);
        }

    }

    @Override
    public void showInternalValidationError() {
        AlertBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.saved))
                .setMessage(getString(R.string.validation_internal_error_datasets))
                .setPositiveButton(getString(R.string.button_ok), () -> {
                    presenter.reopenDataSet();
                    return Unit.INSTANCE;
                })
                .show(getSupportFragmentManager(), AlertBottomDialog.class.getSimpleName());
    }

    @Override
    public void saveAndFinish() {
        Toast.makeText(
                this,
                presenter.isComplete() ? R.string.data_set_quality_check_done : R.string.save,
                Toast.LENGTH_SHORT
        ).show();
        finish();
    }

    public FlowableProcessor<Boolean> observeReopenChanges() {
        return reopenProcessor;
    }
}
