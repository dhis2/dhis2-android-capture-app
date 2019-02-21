package org.dhis2.utils.custom_views.orgUnitCascade;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.android.material.chip.Chip;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.R;
import org.dhis2.data.tuples.Quintet;
import org.dhis2.databinding.DialogCascadeOrgunitBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class OrgUnitCascadeDialog extends DialogFragment {
    private DialogCascadeOrgunitBinding binding;

    private String title;
    private CascadeOrgUnitCallbacks callbacks;
    private CompositeDisposable disposable;
    private List<Quintet<String, String, String, Integer, Boolean>> orgUnits;
    private OrgUnitCascadeAdapter adapter;
    private String selectedOrgUnit;
    private HashMap<String, String> paths;

    public OrgUnitCascadeDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public OrgUnitCascadeDialog setCallbacks(CascadeOrgUnitCallbacks callbacks) {
        this.callbacks = callbacks;
        return this;
    }

    public OrgUnitCascadeDialog setSelectedOrgUnit(String orgUnitUid) {
        this.selectedOrgUnit = orgUnitUid;
        return this;
    }

    public OrgUnitCascadeDialog setOrgUnits(List<OrganisationUnitModel> orgUnits) {
        this.orgUnits = new ArrayList<>();
        this.paths = new HashMap<>();


        if (orgUnits != null)
            for (OrganisationUnitModel orgUnit : orgUnits) { //Users OrgUnits
                parseOrgUnit(orgUnit);
            }
        return this;
    }

    private void parseOrgUnit(OrganisationUnitModel orgUnit) {
        List<String> orgUnitsUid = new ArrayList<>();
        this.orgUnits.add(Quintet.create(orgUnit.uid(),
                orgUnit.displayName(),
                orgUnit.parent() != null ? orgUnit.parent() : "",
                orgUnit.level(),
                true));//OrgUnit Uid, OrgUnit Name, Parent Uid, Level, CanBeSelected
        orgUnitsUid.add(orgUnit.uid());
        paths.put(orgUnit.uid(), orgUnit.path());
        String[] uidPath = orgUnit.path().split("/");
        String[] namePath = orgUnit.displayNamePath().split("/");

        for (int i = 1; i < uidPath.length; i++) {
            if (!uidPath[i].isEmpty() && !uidPath[i].equals(orgUnit.uid())) {
                Quintet<String, String, String, Integer, Boolean> quartet = Quintet.create(uidPath[i], namePath[i], i != 1 ? uidPath[i - 1] : "", i, false); //OrgUnit Uid, OrgUnit Name, Parent Uid, Level, CanBeSelected
                if (!orgUnitsUid.contains(quartet.val0())) {
                    this.orgUnits.add(quartet);
                    orgUnitsUid.add(quartet.val0());
                }
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callbacks.onDialogCancelled();
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

    private void setUpAcceptButton() {
        binding.acceptButton.setOnClickListener(view -> {
            if (binding.recycler.getAdapter() != null) {
                String selectedOrgUnitUid = ((OrgUnitCascadeAdapter) binding.recycler.getAdapter()).getSelectedOrgUnit();
                for (Quintet<String, String, String, Integer, Boolean> orgUnit : orgUnits) {
                    if (orgUnit.val0().equals(selectedOrgUnitUid) && orgUnit.val4()) {
                        callbacks.textChangedConsumer(orgUnit.val0(), orgUnit.val1());
                    }
                }
            }
        });
    }

    private void setUpClearButton() {
        binding.clearButton.setOnClickListener(view -> {
            binding.orgUnitEditText.getText().clear();
            showChips(new ArrayList<>());
            adapter = new OrgUnitCascadeAdapter(orgUnits, canBeSelected -> {
                if (canBeSelected) {
                    binding.acceptButton.setVisibility(View.VISIBLE);
                } else {
                    binding.acceptButton.setVisibility(View.INVISIBLE);
                }
            });
            binding.recycler.setAdapter(adapter);
            binding.acceptButton.setVisibility(View.INVISIBLE);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_cascade_orgunit, container, false);

        binding.orgUnitEditText.setHint(title);

        setUpAcceptButton();

        binding.cancelButton.setOnClickListener(view -> {
            callbacks.onDialogCancelled();
            dismiss();
        });

        setUpClearButton();

        disposable = new CompositeDisposable();

        disposable.add(RxTextView.textChanges(binding.orgUnitEditText)
                .skipInitialValue()
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(data -> data != null && !data.toString().isEmpty() && orgUnits != null && !orgUnits.isEmpty())
                .map(textTofind -> {
                    ArrayList<Quintet<String, String, String, Integer, Boolean>> matches = new ArrayList<>();
                    for (Quintet<String, String, String, Integer, Boolean> quartet : orgUnits)
                        if (quartet.val1().toLowerCase().contains(textTofind.toString()))
                            matches.add(quartet);
                    return matches;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::showChips,
                        Timber::e
                ));

        setUpAdapter();
        binding.recycler.setAdapter(adapter);

        return binding.getRoot();
    }

    private void setUpAdapter() {
        adapter = new OrgUnitCascadeAdapter(orgUnits, canBeSelected -> {
            if (canBeSelected) {
                binding.acceptButton.setVisibility(View.VISIBLE);
            } else {
                binding.acceptButton.setVisibility(View.INVISIBLE);
            }
        });

        if (selectedOrgUnit != null) {
            for (Quintet<String, String, String, Integer, Boolean> orgUnit : orgUnits) {
                if (orgUnit.val0().equals(selectedOrgUnit)) {
                    adapter.setOrgUnit(orgUnit, paths.get(orgUnit.val0()));
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    private void showChips(ArrayList<Quintet<String, String, String, Integer, Boolean>> data) {
        binding.results.removeAllViews();
        for (Quintet<String, String, String, Integer, Boolean> trio : data) {
            if (trio.val4() && getContext() != null) { //Only shows selectable orgUnits
                Chip chip = new Chip(getContext());
                chip.setText(trio.val1());
                chip.setOnClickListener(view -> {
                    callbacks.textChangedConsumer(trio.val0(), trio.val1());
                    dismiss();
                });
                binding.results.addView(chip);
            }
        }
    }

    public interface CascadeOrgUnitCallbacks {
        void textChangedConsumer(String selectedOrgUnitUid, String selectedOrgUnitName);

        void onDialogCancelled();
    }

    @Override
    public void dismiss() {
        disposable.clear();
        super.dismiss();
    }
}
