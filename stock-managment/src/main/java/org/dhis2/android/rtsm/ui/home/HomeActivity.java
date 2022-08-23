package org.dhis2.android.rtsm.ui.home;


import static org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.OperationState;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.data.persistence.UserActivity;
import org.dhis2.android.rtsm.databinding.ActivityHomeBinding;
import org.dhis2.android.rtsm.ui.base.BaseActivity;
import org.dhis2.android.rtsm.ui.base.GenericListAdapter;
import org.dhis2.android.rtsm.ui.managestock.ManageStockActivity;
import org.dhis2.android.rtsm.utils.DateUtils;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

@AndroidEntryPoint
public class HomeActivity extends BaseActivity {
    private ActivityHomeBinding binding;

    private AutoCompleteTextView facilityTextView;
    private AutoCompleteTextView distributedToTextView;
    private RecyclerView recentActivitiesRecyclerView;

    private HomeViewModel viewModel;
    private RecentActivityAdapter recentActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = (HomeViewModel) getViewModel();

        binding = (ActivityHomeBinding) getViewBinding();
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        facilityTextView = (AutoCompleteTextView) binding.selectedFacilityTextView.getEditText();
        distributedToTextView = (AutoCompleteTextView) binding.distributedToTextView.getEditText();
        recentActivitiesRecyclerView = binding.recentActivityList;

        attachObservers();
        setupComponents();

        // Cannot go up the stack
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean showMoreOptions() {
        return true;
    }

    private void attachObservers() {
        viewModel.getFacilities().observe(this, operationState -> {
            if (reportNetworkError(operationState)) return;

            if (operationState instanceof OperationState.Success) {
                List<OrganisationUnit> facilities =
                        ((OperationState.Success<List<OrganisationUnit>>) operationState).getResult();
                facilityTextView.setAdapter(
                        new GenericListAdapter<>(this, R.layout.list_item, facilities));
            }
        });

        viewModel.getDestinationsList().observe(this, operationState -> {
            if (reportNetworkError(operationState)) return;

            if (operationState instanceof OperationState.Success) {
                List<Option> destinations = ((OperationState.Success<List<Option>>) operationState).getResult();
                distributedToTextView.setAdapter(
                        new GenericListAdapter<>(
                                this, R.layout.list_item, destinations
                        ));
            }
        });

        viewModel.getDestination().observe(this, destination -> {
            if (destination == null) {
                distributedToTextView.getEditableText().clear();
            }
        });
    }

    private <T> boolean reportNetworkError(OperationState<T> operationState) {
        if (operationState.getClass() == OperationState.Error.class) {
            displayError(binding.getRoot(),
                    ((OperationState.Error) operationState).getErrorStringRes());
            return true;
        }
        return false;
    }

    private void setupComponents() {
        setupButtons();

        viewModel.getFacility().observe(this, ou -> facilityTextView.setText(ou.displayName()));

        facilityTextView.setOnItemClickListener((adapterView, view, position, rowId) ->
                viewModel.setFacility((OrganisationUnit) facilityTextView.getAdapter().getItem(position))
        );

        distributedToTextView.setOnItemClickListener((adapterView, view, position, rowId) ->
                viewModel.setDestination(
                        (Option) distributedToTextView.getAdapter().getItem(position))
        );

        binding.fabManageStock.setOnClickListener(view ->
                navigateToManageStock(
                        getIntent().getParcelableExtra(INTENT_EXTRA_APP_CONFIG)
                )
        );
        setupTransactionDateField();
        setupRecentActivities();
    }

    private void setupButtons() {
        // Add listeners to the buttons
        Map<TransactionType, MaterialButton> buttonsMap = new HashMap<>();
        buttonsMap.put(TransactionType.DISTRIBUTION, binding.distributionButton);
        buttonsMap.put(TransactionType.DISCARD, binding.discardButton);
        buttonsMap.put(TransactionType.CORRECTION, binding.correctionButton);

        buttonsMap.entrySet().iterator().forEachRemaining(entry -> {
            TransactionType type = entry.getKey();
            MaterialButton button = entry.getValue();

            button.setOnClickListener(view -> selectTransaction(type));
        });
    }

    private void selectTransaction(TransactionType buttonTransaction) {
        viewModel.selectTransaction(buttonTransaction);

        updateTheme(buttonTransaction);
    }

    private void updateTheme(TransactionType type) {
        int color;

        switch (type) {
            case DISTRIBUTION:
                color = R.color.distribution_color;
                break;
            case DISCARD:
                color = R.color.discard_color;
                break;
            case CORRECTION:
                color = R.color.correction_color;
                break;
            default:
                color = -1;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && color != -1) {
            ColorStateList colorStateList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, color)
            );
            binding.fabManageStock.setBackgroundTintBlendMode(BlendMode.SRC_OVER);
            binding.toolbar.setBackgroundTintBlendMode(BlendMode.SRC_OVER);
            binding.fabManageStock.setBackgroundTintList(colorStateList);
            binding.toolbar.setBackgroundTintList(colorStateList);
        }
    }

    private void setupTransactionDateField() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setCalendarConstraints(DateUtils.getMonthStartToNowConstraint())
                .build();

        picker.addOnPositiveButtonClickListener(viewModel::setTransactionDate);

        // Show the date picker when the calendar icon is clicked
        binding.transactionDateTextView.setEndIconOnClickListener(view -> {
            Dialog pickerDialog = picker.getDialog();
            if (!(pickerDialog != null && pickerDialog.isShowing()))
                picker.show(getSupportFragmentManager(), MaterialDatePicker.class.getCanonicalName());
        });
    }

    private void setupRecentActivities() {
        recentActivityAdapter = new RecentActivityAdapter();
        recentActivitiesRecyclerView.setAdapter(recentActivityAdapter);
        viewModel.getRecentActivities().observe(this, operationState -> {
            // Loading
            if (operationState == OperationState.Loading.INSTANCE) {
                showLoadingRecentActivities();
                return;
            }

            // Error
            if (operationState.getClass() == OperationState.Error.class) {
                showRecentActivitiesError(operationState);
                return;
            }

            // Success
            if (operationState.getClass() == OperationState.Success.class) {
                showRecentActivities(operationState);
            }
        });

        recentActivitiesRecyclerView.addItemDecoration(
                new RecentActivityItemDividerDecoration(
                        getApplicationContext(), R.drawable.vertical_line_divider));
    }

    private void resetRecentActivitiesState() {
        binding.recentActivityList.setVisibility(View.INVISIBLE);
        binding.recentActivityMessageTextview.setCompoundDrawablesWithIntrinsicBounds(
                null, null, null, null);
        binding.recentActivityMessageTextview.setText("");
        binding.recentActivityMessageTextview.setTextAppearance(R.style.ListNormal);
    }

    private void showRecentActivitiesError(OperationState<List<UserActivity>> operationState) {
        resetRecentActivitiesState();

        binding.recentActivityMessageTextview.setCompoundDrawablesWithIntrinsicBounds(
                null,
                AppCompatResources.getDrawable(this, R.drawable.ic_error),
                null,
                null
        );

        int errorRes = ((OperationState.Error) operationState).getErrorStringRes();
        binding.recentActivityMessageTextview.setText(errorRes);
        binding.recentActivityMessageTextview.setTextAppearance(R.style.ListError);
    }

    private void showRecentActivities(OperationState<List<UserActivity>> operationState) {
        resetRecentActivitiesState();

        List<UserActivity> activities =
                ((OperationState.Success<List<UserActivity>>) operationState).getResult();

        if (!activities.isEmpty()) {
            binding.recentActivityList.setVisibility(View.VISIBLE);
            recentActivityAdapter.submitList(activities);
        } else {
            binding.recentActivityMessageTextview.setText(R.string.recent_activities_empty_message);
            binding.recentActivityMessageTextview.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    AppCompatResources.getDrawable(this, R.drawable.ic_empty_list),
                    null,
                    null
            );
        }
    }

    private void showLoadingRecentActivities() {
        resetRecentActivitiesState();

        binding.recentActivityMessageTextview.setCompoundDrawablesWithIntrinsicBounds(
                null,
                AppCompatResources.getDrawable(this, R.drawable.ic_loading_items),
                null,
                null
        );
        binding.recentActivityMessageTextview.setCompoundDrawablePadding(32);
        binding.recentActivityMessageTextview.setText(R.string.recent_activities_loading_message);
    }

    private void navigateToManageStock(AppConfig appConfig) {
        Integer fieldError = viewModel.checkForFieldErrors();
        if (fieldError != null) {
            displayError(binding.getRoot(), fieldError);
            return;
        }

        startActivity(
                ManageStockActivity.getManageStockActivityIntent(
                        this,
                        viewModel.getData(),
                        appConfig
                )
        );
    }

    public static Intent getHomeActivityIntent(Context context, AppConfig config) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(INTENT_EXTRA_APP_CONFIG, config);
        return intent;
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        // TODO: Handle errors that can occur if expected configuration properties
        //  (e.g. program id, item code id etc) weren't found.
        //  The application cannot proceed without them
        return new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_home);
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return ((ActivityHomeBinding) getViewBinding()).toolbar;
    }
}