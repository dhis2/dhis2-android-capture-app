package org.dhis2.android.rtsm.ui.managestock;

import static org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG;
import static org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_TRANSACTION;
import static org.dhis2.android.rtsm.utils.Utils.isValidStockOnHand;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.OperationState;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.data.models.StockItem;
import org.dhis2.android.rtsm.data.models.Transaction;
import org.dhis2.android.rtsm.databinding.ActivityManageStockBinding;
import org.dhis2.android.rtsm.ui.base.BaseActivity;
import org.dhis2.android.rtsm.ui.base.BaseViewModel;
import org.dhis2.android.rtsm.ui.base.ItemWatcher;
import org.dhis2.android.rtsm.ui.reviewstock.ReviewStockActivity;
import org.dhis2.android.rtsm.utils.ActivityManager;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

@AndroidEntryPoint
public class ManageStockActivity extends BaseActivity {
    private ActivityManageStockBinding binding;
    private ManageStockViewModel viewModel;
    private ManageStockAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = (ManageStockViewModel) getViewModel();

        binding = (ActivityManageStockBinding) getViewBinding();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        binding.fabReviewStock.setOnClickListener(view ->
                navigateToReviewStock(
                        getIntent().getParcelableExtra(INTENT_EXTRA_APP_CONFIG)
                )
        );

        setTitle(viewModel.getTransaction().getTransactionType());

        // Enable the ability to show the info guide when the transaction is of Correction type
        if (viewModel.getTransaction().getTransactionType() == TransactionType.CORRECTION) {
            binding.stockEntriesTableHeader.qtyInfoIconButton.setVisibility(View.VISIBLE);
        }

        setupSearchInput();
        setupRecyclerView();
        setupObservers();
        configureScanner();
        updateNextButton();
    }

    private final ItemWatcher<StockItem, String, String> itemWatcher =
            new ItemWatcher<StockItem, String, String>() {

                @Override
                public void updateFields(StockItem item, @Nullable String qty, int position,
                                         @NonNull List<? extends RuleEffect> ruleEffects) {
                    ruleEffects.forEach(ruleEffect -> {
                        if (ruleEffect.ruleAction() instanceof RuleActionAssign &&
                                (((RuleActionAssign) ruleEffect.ruleAction()).field()
                                        .equals(viewModel.getConfig().getStockOnHand()))) {

                            String value = ruleEffect.data();
                            boolean isValid = isValidStockOnHand(value);
                            String stockOnHand = isValid ? value : item.getStockOnHand();

                            viewModel.addItem(item, qty, stockOnHand, !isValid);
                            if (!isValid) {
                                displayError(binding.getRoot(), R.string.stock_on_hand_exceeded_message);
                            }

                            updateItemView(position);
                            updateNextButton();
                        }
                    });

                    updateNextButton();
                }

                @Override
                public String getStockOnHand(StockItem item) {
                    return viewModel.getStockOnHand(item);
                }

                @Override
                public void quantityChanged(StockItem item, int position, @Nullable String value,
                                            @Nullable OnQuantityValidated callback) {
                    // If the qty is cleared, remove from cache if already present
                    if (value == null || value.isEmpty()) {
                        boolean outcome = viewModel.removeItemFromCache(item);
                        if (outcome) {
                            updateItemView(position);
                            updateNextButton();
                        }
                        return;
                    }

                    viewModel.setQuantity(item, position, value, callback);
                }

                @Override
                public void removeItem(StockItem item) {
                }

                @Nullable
                @Override
                public String getQuantity(StockItem item) {
                    return viewModel.getItemQuantity(item);
                }

                @Override
                public boolean hasError(StockItem item) {
                    return viewModel.hasError(item);
                }
            };

    private void updateItemView(int position) {
        runOnUiThread(() -> adapter.notifyItemRangeChanged(position, 1));
    }

    private void updateNextButton() {
        runOnUiThread(() -> binding.fabReviewStock.setEnabled(viewModel.canReview()));
    }

    @Override
    public void onVoiceInputStateChanged() {
        super.onVoiceInputStateChanged();
        adapter.voiceInputStateChanged(getVoiceInputEnabled());
    }

    @Override
    public void onBackPressed() {
        if (viewModel.getItemCount() == 0) {
            super.onBackPressed();
            return;
        }

        ActivityManager.showBackButtonWarning(this, () -> {
            super.onBackPressed();
            return null;
        });
    }

    private void configureScanner() {
        ActivityResultLauncher<ScanOptions> barcodeLauncher =
                registerForActivityResult(new ScanContract(), scanIntentResult -> {
                    if (scanIntentResult.getContents() == null) {
                        ActivityManager.showToast(this, R.string.scan_canceled);
                    } else {
                        onScanCompleted(
                                scanIntentResult,
                                binding.searchFieldLayout.searchInputField,
                                binding.stockItemsList
                        );
                    }
                });
        binding.scanButton.setOnClickListener(view -> scanBarcode(barcodeLauncher));
    }

    private void setupObservers() {
        viewModel.getOperationState().observe(this, this::updateState);

        viewModel.getStockItems().observe(this, pagedListLiveData -> {
            adapter.submitList(pagedListLiveData);

            // Scroll back to the top of the recyclerview if a new paged list is added
            resetListScrollPosition();

            // Handle empty results state
            updateStocklistInfoBox(R.drawable.ic_empty_list, R.string.no_items_found,
                    pagedListLiveData.isEmpty());

            // TODO: Handle error states
        });

        viewModel.getShowGuide().observe(this,
                showGuide -> crossFade(binding.qtyGuide.getRoot(), showGuide,
                        getResources().getInteger(android.R.integer.config_shortAnimTime)));
    }

    private void resetListScrollPosition() {
        LinearLayoutManager layoutManager =
                (LinearLayoutManager) binding.stockItemsList.getLayoutManager();
        if (layoutManager != null) {
            int position = layoutManager.findFirstCompletelyVisibleItemPosition();
            if (position != RecyclerView.NO_POSITION) {
                binding.stockItemsList.scrollToPosition(position);
            }
        }
    }

    private void updateStocklistInfoBox(Integer drawableRes, Integer stringRes, boolean show) {
        if (drawableRes != null) {
            binding.noResultsTextView.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    ContextCompat.getDrawable(this, drawableRes),
                    null,
                    null
            );
        } else {
            binding.noResultsTextView.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, null, null);
        }

        if (stringRes != null) {
            binding.noResultsTextView.setText(stringRes);
        } else {
            binding.noResultsTextView.setText("");
        }

        binding.noResultsTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private <T> void updateState(OperationState<T> operationState) {
        if (operationState == OperationState.Loading.INSTANCE) {
            updateStocklistInfoBox(R.drawable.ic_loading_items, R.string.loading_items_message, true);
            return;
        }

        if (operationState.getClass() == OperationState.Completed.class) {
            updateStocklistInfoBox(null, null, false);
        }
    }

    @Override
    public boolean showMoreOptions() {
        return true;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Integer getCustomTheme(@NotNull ViewModel viewModel) {
        switch (((ManageStockViewModel) viewModel).getTransaction().getTransactionType()) {
            case DISTRIBUTION:
                return R.style.Theme_App_Distribution;
            case DISCARD:
                return R.style.Theme_App_Discard;
            case CORRECTION:
                return R.style.Theme_App_Correction;
            default:
                return null;
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.stockItemsList;

        adapter = new ManageStockAdapter(
                itemWatcher,
                getSpeechController(),
                viewModel.getConfig(),
                viewModel.getTransaction(),
                getVoiceInputEnabled()
        );
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );

        // Set up listeners for the guide info box
        binding.stockEntriesTableHeader.qtyInfoIconButton.setOnClickListener(v -> viewModel.toggleGuideDisplay());
        binding.qtyGuide.closeGuideButton.setOnClickListener(v -> viewModel.toggleGuideDisplay());
    }

    private void setupSearchInput() {
        TextInputEditText searchInputField = binding.searchFieldLayout.searchInputField;
        searchInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.onSearchQueryChanged(editable.toString());
            }
        });
    }

    @NonNull
    @Override
    public BaseViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        return new ViewModelProvider(this).get(ManageStockViewModel.class);
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_manage_stock);
    }

    private void navigateToReviewStock(AppConfig appConfig) {
        startActivity(
                ReviewStockActivity.getReviewStockActivityIntent(
                        this,
                        viewModel.getData(),
                        appConfig
                )
        );
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return ((ActivityManageStockBinding) getViewBinding()).toolbarContainer.toolbar;
    }

    public static Intent getManageStockActivityIntent(
            Context context,
            Transaction bundle,
            AppConfig appConfig
    ) {
        Intent intent = new Intent(context, ManageStockActivity.class);
        intent.putExtra(INTENT_EXTRA_TRANSACTION, bundle);
        intent.putExtra(INTENT_EXTRA_APP_CONFIG, appConfig);
        return intent;
    }
}