package org.dhis2.usescases.datasets.dataSetTable;

import static org.dhis2.commons.extensions.ViewExtensionsKt.closeKeyboard;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jakewharton.rxbinding2.view.RxView;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.dialogs.AlertBottomDialog;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.data.dhislogic.DhisPeriodUtils;
import org.dhis2.databinding.ActivityDatasetTableBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSection;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionKt;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.commons.Constants;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.dhis2.utils.validationrules.ValidationResultViolationsAdapter;
import org.dhis2.utils.validationrules.Violation;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.period.Period;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import kotlin.Unit;

public class DataSetTableActivity extends ActivityGlobalAbstract implements DataSetTableContract.View {

    String orgUnitUid;
    String orgUnitName;
    String periodTypeName;
    String periodInitialDate;
    String catOptCombo;
    String dataSetUid;
    String periodId;

    boolean accessDataWrite;

    @Inject
    DataSetTableContract.Presenter presenter;
    @Inject
    DhisPeriodUtils periodUtils;

    private ActivityDatasetTableBinding binding;
    private DataSetSectionAdapter viewPagerAdapter;
    private boolean backPressed;
    private DataSetTableComponent dataSetTableComponent;

    private BottomSheetBehavior<View> behavior;
    private FlowableProcessor<Boolean> reopenProcessor;
    private boolean isKeyboardOpened = false;

    private static final int MAX_ITEM_CACHED_VIEWPAGER2 = 2;
    private static final String DATAVALUE_SYNC = "DATAVALUE_SYNC";

    public static Bundle getBundle(@NonNull String dataSetUid,
                                   @NonNull String orgUnitUid,
                                   @NonNull String orgUnitName,
                                   @NonNull String periodTypeName,
                                   @NonNull String periodInitialDate,
                                   @NonNull String periodId,
                                   @NonNull String catOptCombo) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        bundle.putString(Constants.ORG_UNIT, orgUnitUid);
        bundle.putString(Constants.ORG_UNIT_NAME, orgUnitName);
        bundle.putString(Constants.PERIOD_TYPE, periodTypeName);
        bundle.putString(Constants.PERIOD_TYPE_DATE, periodInitialDate);
        bundle.putString(Constants.PERIOD_ID, periodId);
        bundle.putString(Constants.CAT_COMB, catOptCombo);
        return bundle;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        orgUnitUid = getIntent().getStringExtra(Constants.ORG_UNIT);
        orgUnitName = getIntent().getStringExtra(Constants.ORG_UNIT_NAME);
        periodTypeName = getIntent().getStringExtra(Constants.PERIOD_TYPE);
        periodId = getIntent().getStringExtra(Constants.PERIOD_ID);
        periodInitialDate = getIntent().getStringExtra(Constants.PERIOD_TYPE_DATE);
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
        setViewPager();
        presenter.init(orgUnitUid, periodTypeName, catOptCombo, periodInitialDate, periodId);
        binding.navigationView.setOnNavigationItemSelectedListener(item -> {
            int pagePosition = viewPagerAdapter.getNavigationPagePosition(item.getItemId());
            binding.viewPager.postDelayed(() -> {
                binding.tabLayout.setVisibility(pagePosition == 0 ? View.GONE : View.VISIBLE);
                binding.viewPager.setCurrentItem(pagePosition);
            }, 100);
            return true;
        });

        binding.syncButton.setOnClickListener(view-> showGranularSync());
    }

    private void showGranularSync(){
        presenter.onClickSyncStatus();
        SyncStatusDialog syncDialog = new SyncStatusDialog.Builder()
                .setConflictType(SyncStatusDialog.ConflictType.DATA_VALUES)
                .setUid(dataSetUid)
                .setPeriodId(periodId)
                .setOrgUnit(orgUnitUid)
                .setAttributeOptionCombo(catOptCombo)
                .onDismissListener(hasChanged -> {
                    if(hasChanged) presenter.updateData();
                })
                .build();

        syncDialog.show(getSupportFragmentManager(), DATAVALUE_SYNC);
    }

    @Override
    public void startInputEdition(){
        isKeyboardOpened = true;
        binding.navigationView.setVisibility(View.GONE);
        binding.saveButton.hide();
        if (binding.BSLayout.bottomSheetLayout.getVisibility() == View.VISIBLE) {
            if (behavior != null && behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    @Override
    public void finishInputEdition(){
        isKeyboardOpened = false;
        new Handler().postDelayed(()->{
            binding.navigationView.setVisibility(View.VISIBLE);
            binding.saveButton.show();
        },1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDettach();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        viewPagerAdapter.notifyDataSetChanged();
    }

    private void setViewPager() {
        viewPagerAdapter = new DataSetSectionAdapter(this,
                accessDataWrite,
                getIntent().getStringExtra(Constants.DATA_SET_UID),
                orgUnitUid,
                periodId,
                catOptCombo);
        binding.viewPager.setUserInputEnabled(false);
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.viewPager.setOffscreenPageLimit(MAX_ITEM_CACHED_VIEWPAGER2);
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    binding.syncButton.setVisibility(View.VISIBLE);
                } else {
                    binding.syncButton.setVisibility(View.GONE);
                }
            }
        });

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.dataset_overview);
                tab.view.setVisibility(View.GONE);
            } else {
                tab.setText(viewPagerAdapter.getSectionTitle(position));
            }
        }).attach();
    }

    @Override
    public void setSections(List<DataSetSection> sections) {
        viewPagerAdapter.swapData(
                DataSetSectionKt.replaceNoSection(sections,getString(R.string.dataset_data))
        );
        binding.navigationView.selectItemAt(1);
        binding.viewPager.setCurrentItem(1);
    }

    public void updateTabLayout() {

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
    public String getOrgUnitName() {
        return orgUnitName;
    }

    @Override
    public void renderDetails(DataSet dataSet, String catComboName, Period period, boolean isComplete) {
        binding.dataSetName.setText(dataSet.displayName());
        StringBuilder subtitle = new StringBuilder(
                periodUtils.getPeriodUIString(period.periodType(), period.startDate(), Locale.getDefault())
        )
                .append(" | ")
                .append(orgUnitName);
        if (!catComboName.equals("default")) {
            subtitle.append(" | ")
                    .append(catComboName);
        }
        binding.dataSetSubtitle.setText(subtitle);
    }

    public void update() {
        presenter.init(orgUnitUid, periodTypeName, catOptCombo, periodInitialDate, periodId);
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
                        binding.saveButton.show();
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
        Snackbar.make(binding.viewPager, R.string.dataset_completed, BaseTransientBottomBar.LENGTH_SHORT)
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
                new Handler().postDelayed(() -> {
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
