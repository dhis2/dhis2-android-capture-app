package org.dhis2.android.rtsm.ui.reviewstock;


import static org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG;
import static org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_MESSAGE;
import static org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_STOCK_ENTRIES;
import static org.dhis2.android.rtsm.utils.Utils.isValidStockOnHand;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.data.models.StockEntry;
import org.dhis2.android.rtsm.databinding.ActivityReviewStockBinding;
import org.dhis2.android.rtsm.ui.base.BaseActivity;
import org.dhis2.android.rtsm.ui.base.ItemWatcher;
import org.dhis2.android.rtsm.ui.home.HomeActivity;
import org.dhis2.android.rtsm.utils.ActivityManager;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

@AndroidEntryPoint
public class ReviewStockActivity extends BaseActivity {

    private ReviewStockViewModel viewModel;
    private ActivityReviewStockBinding binding;
    private ReviewStockAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = (ReviewStockViewModel) getViewModel();
        binding = (ActivityReviewStockBinding) getViewBinding();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        setTitle(viewModel.getTransaction().getTransactionType());

        // Enable the ability to show the info guide when the transaction is of Correction type
        if (viewModel.getTransaction().getTransactionType() == TransactionType.CORRECTION) {
            binding.stockEntriesTableHeader.qtyInfoIconButton.setVisibility(View.VISIBLE);
        }

        setupSearchInput();
        setupRecyclerView();
        configureScanner();

        binding.fabCommitStock.setOnClickListener(view -> viewModel.commitTransaction());

        viewModel.getCommitStatus().observe(this, status -> {
            if (status)
                navigateToHome();
        });

        viewModel.getShowGuide().observe(this,
                showGuide -> crossFade(binding.qtyGuide.getRoot(), showGuide,
                        getResources().getInteger(android.R.integer.config_shortAnimTime)));

        updateCommitButton();
    }

    private final ItemWatcher<StockEntry, String, String> itemWatcher =
            new ItemWatcher<StockEntry, String, String>() {

                @Override
                public void quantityChanged(StockEntry item, int position, @Nullable String value,
                                            @Nullable OnQuantityValidated callback) {
                    viewModel.setQuantity(item, position, value, callback);
                }

                @Override
                public void updateFields(StockEntry item, @Nullable String qty, int position,
                                         @NonNull List<? extends RuleEffect> ruleEffects) {
                    ruleEffects.forEach(ruleEffect -> {
                        if (ruleEffect.ruleAction() instanceof RuleActionAssign &&
                                (((RuleActionAssign) ruleEffect.ruleAction()).field()
                                        .equals(viewModel.getConfig().getStockOnHand()))) {

                            String value = ruleEffect.data();
                            boolean isValidStockOnHand = isValidStockOnHand(value);
                            boolean isValidQty = !(qty == null || qty.isEmpty());
                            boolean isValid = isValidStockOnHand && isValidQty;

                            String stockOnHand = isValid ? value : item.getStockOnHand();
                            viewModel.updateItem(item, qty, stockOnHand, !isValid);

                            if (!isValidStockOnHand) {
                                displayError(binding.getRoot(), R.string.stock_on_hand_exceeded_message);
                            }

                            if (!isValidQty) {
                                displayError(binding.getRoot(), R.string.reviewed_item_cannot_be_empty_message);
                            }

                            updateCommitButton();
                        }
                    });

                    updateItemView(position);
                    updateCommitButton();
                }

                @Override
                public void removeItem(StockEntry item) {
                    viewModel.removeItem(item);
                    updateCommitButton();
                }

                @Nullable
                @Override
                public String getStockOnHand(StockEntry item) {
                    return viewModel.getItemStockOnHand(item);
                }

                @Override
                public String getQuantity(StockEntry item) {
                    return viewModel.getItemQuantity(item);
                }

                @Override
                public boolean hasError(StockEntry item) {
                    return item.getHasError();
                }
            };

    private void updateCommitButton() {
        runOnUiThread(() -> binding.fabCommitStock.setEnabled(viewModel.canCommit()));
    }

    @Override
    public void onBackPressed() {
        Integer itemsCount = viewModel.getReviewedItemsCount().getValue();
        if (itemsCount != null && itemsCount == 0) {
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
                        ActivityManager.showInfoMessage(binding.getRoot(),
                                getString(R.string.scan_canceled));
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

    private void navigateToHome() {
        Intent intent = HomeActivity.getHomeActivityIntent(
                this,
                getIntent().getParcelableExtra(INTENT_EXTRA_APP_CONFIG)
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(INTENT_EXTRA_MESSAGE, getString(R.string.transaction_completed));
        ActivityManager.startActivity(this, intent, true);
    }

    @Override
    public boolean showMoreOptions() {
        return true;
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.stockItemsList;
        recyclerView.setHasFixedSize(true);

        adapter = new ReviewStockAdapter(
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

        viewModel.getReviewedItems().observe(this, adapter::submitList);

        // Set up listeners for the guide info box
        binding.stockEntriesTableHeader.qtyInfoIconButton.setOnClickListener(v -> viewModel.toggleGuideDisplay());
        binding.qtyGuide.closeGuideButton.setOnClickListener(v -> viewModel.toggleGuideDisplay());
    }

    private void setupSearchInput() {
        TextInputEditText searchField = binding.searchFieldLayout.searchInputField;
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.onSearchQueryChanged(editable.toString());
            }
        });
    }

    @Nullable
    @Override
    public Integer getCustomTheme(@NonNull ViewModel viewModel) {
        switch (((ReviewStockViewModel) viewModel).getTransaction().getTransactionType()) {
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

    public static Intent getReviewStockActivityIntent(
            Context context,
            Parcelable bundle,
            AppConfig appConfig
    ) {
        Intent intent = new Intent(context, ReviewStockActivity.class);
        intent.putExtra(INTENT_EXTRA_STOCK_ENTRIES, bundle);
        intent.putExtra(INTENT_EXTRA_APP_CONFIG, appConfig);
        return intent;
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        return new ViewModelProvider(this).get(ReviewStockViewModel.class);
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_review_stock);
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return ((ActivityReviewStockBinding) getViewBinding()).toolbarContainer.toolbar;
    }

    private void updateItemView(int position) {
        runOnUiThread(() -> adapter.notifyItemRangeChanged(position, 1));
    }

    @Override
    public void onVoiceInputStateChanged() {
        super.onVoiceInputStateChanged();
        adapter.voiceInputStateChanged(getVoiceInputEnabled());
    }
}
