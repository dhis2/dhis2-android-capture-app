package org.dhis2.utils.custom_views.orgUnitCascade;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.DialogCascadeOrgunitBinding;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class OrgUnitCascadeDialog extends DialogFragment {
    private final OUSelectionType ouSelectionType;
    private DialogCascadeOrgunitBinding binding;

    public enum OUSelectionType {CAPTURE, SEARCH}

    private String title;
    private CascadeOrgUnitCallbacks callbacks;
    private CompositeDisposable disposable;
    private String selectedOrgUnit;
    private D2 d2;
    private List<OrgUnitItem> initialData;

    public OrgUnitCascadeDialog(String fieldLabel, String currentOUUid, CascadeOrgUnitCallbacks cascadeOrgUnitCallbacks, OUSelectionType ouSelectionType) {
        this.callbacks = cascadeOrgUnitCallbacks;
        this.title = fieldLabel;
        this.selectedOrgUnit = currentOUUid;
        this.ouSelectionType = ouSelectionType;
    }


    public OrgUnitCascadeDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public void onCancel(@NotNull DialogInterface dialog) {
        super.onCancel(dialog);
        callbacks.onDialogCancelled();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        d2 = ((App) context.getApplicationContext()).serverComponent().userManager().getD2();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_cascade_orgunit, container, false);
        binding.title.setText(title);
        disposable = new CompositeDisposable();

        setListeners();


        disposable.add(RxTextView.textChanges(binding.orgUnitSearchEditText)
                .skipInitialValue()
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(data -> !isEmpty(data))
                .map(textTofind -> d2.organisationUnitModule().organisationUnits.byDisplayName().like("%" + textTofind.toString() + "%").blockingGet())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::showChips,
                        Timber::e
                ));

        disposable.add(
                Observable.fromCallable(() -> d2.organisationUnitModule().organisationUnits.blockingGet())
                        .map(ouList -> {
                            int maxLevel = -1;
                            for (OrganisationUnit ou : ouList) {
                                if (maxLevel < ou.level())
                                    maxLevel = ou.level();
                            }
                            return maxLevel;
                        })
                        .map(maxLevel -> {
                            List<OrgUnitItem> orgUnitItems = new ArrayList<>();
                            for (int i = 1; i <= maxLevel; i++) {
                                OrgUnitItem orgUnitItem = new OrgUnitItem(d2.organisationUnitModule().organisationUnits, ouSelectionType);
                                orgUnitItem.setMaxLevel(maxLevel);
                                orgUnitItem.setLevel(i);
                                orgUnitItem.setOrganisationUnitLevel(d2.organisationUnitModule().organisationUnitLevels.byLevel().eq(i).one().blockingGet());//TODO: CHECK IF OU ALREADY SELECTED
                                orgUnitItems.add(orgUnitItem);
                            }
                            return orgUnitItems;
                        })
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::setAdapter,
                                Timber::e
                        )
        );
        setRetainInstance(true);
        return binding.getRoot();
    }

    private void setAdapter(List<OrgUnitItem> data) {
        initialData = data;
        OrgUnitCascadeAdapter adapter = new OrgUnitCascadeAdapter(data, selectedOrgUnit, canBeSelected -> {
            if (canBeSelected) {
                binding.acceptButton.setVisibility(View.VISIBLE);
            } else {
                binding.acceptButton.setVisibility(View.INVISIBLE);
            }
        }, d2.organisationUnitModule().organisationUnits, ouSelectionType);
        binding.recycler.setAdapter(adapter);
    }

    private void setListeners() {
        binding.acceptButton.setOnClickListener(view -> {
            if (binding.recycler.getAdapter() != null) {
                binding.orgUnitSearchEditText.getText().clear();
                showChips(new ArrayList<>());
                String selectedOrgUnitUid = ((OrgUnitCascadeAdapter) binding.recycler.getAdapter()).getSelectedOrgUnit();
                callbacks.textChangedConsumer(selectedOrgUnitUid, d2.organisationUnitModule().organisationUnits.uid(selectedOrgUnitUid).blockingGet().displayName());
                dismiss();
            }
        });
        binding.cancelButton.setOnClickListener(view -> {
            callbacks.onDialogCancelled();
            dismiss();
        });
        binding.clearButton.setOnClickListener(view -> {
            binding.orgUnitSearchEditText.getText().clear();
            showChips(new ArrayList<>());
            setAdapter(initialData);
            binding.acceptButton.setVisibility(View.INVISIBLE);
            callbacks.onClear();
            dismiss();
        });
    }

    private void showChips(List<OrganisationUnit> orgUnits) {
        binding.results.removeAllViews();
        for (OrganisationUnit ou : orgUnits) {
            Chip chip = new Chip(getContext());

            String level = "";
            OrganisationUnitLevel ouLevel = d2.organisationUnitModule().organisationUnitLevels.byLevel().eq(ou.level()).one().blockingGet();
            if (ouLevel != null) {
                level = ouLevel.displayName() + " : ";
            } else
                level = "Lvl. " + ou.level() + " : ";

            chip.setText(String.format("%s%s", level, ou.displayName()));
            chip.setOnClickListener(view -> {
                callbacks.textChangedConsumer(ou.uid(), ou.displayName());
                dismiss();
            });
            chip.setChipMinHeightResource(R.dimen.chip_minHeight);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                chip.setElevation(6f);
            }
            chip.setChipBackgroundColorResource(R.color.white);
            binding.results.addView(chip);

        }
    }

    public interface CascadeOrgUnitCallbacks {
        void textChangedConsumer(String selectedOrgUnitUid, String selectedOrgUnitName);

        void onDialogCancelled();

        void onClear();
    }

    @Override
    public void dismiss() {
        binding.orgUnitSearchEditText.getText().clear();
        disposable.clear();
        super.dismiss();
    }
}
