package org.dhis2.utils.custom_views.orgUnitCascade;

import android.app.Dialog;
import android.content.DialogInterface;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.chip.Chip;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.tuples.Quintet;
import org.dhis2.databinding.DialogCascadeOrgunitBinding;
import org.dhis2.utils.custom_views.TextInputAutoCompleteTextView;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class OrgUnitCascadeDialog extends DialogFragment {
    DialogCascadeOrgunitBinding binding;

    private String title;
    private CascadeOrgUnitCallbacks callbacks;
    private CompositeDisposable disposable;
    private List<Quintet<String, String, String, Integer, Boolean>> orgUnits;
    private OrgUnitCascadeAdapter adapter;
    private static String selectedOrgUnit;
    private HashMap<String, String> paths;
    private List<OrganisationUnitLevel> levels;
    private static OrgUnitCascadeDialog instance;

    public static OrgUnitCascadeDialog getInstance(){
        if(instance != null)
            return instance;

        return null;
    }

    public static OrgUnitCascadeDialog newInstance(OrgUnitViewModel model, List<OrganisationUnitModel> orgUnits,
                                                  List<OrganisationUnitLevel> levels, FlowableProcessor<RowAction> processor,
                                                  TextInputAutoCompleteTextView editText) {
        if(instance == null)
            instance = new OrgUnitCascadeDialog();

        instance.setTitle(model.label())
            .setOrgUnits(orgUnits)
            .setLevels(levels)
            .setCallbacks(new OrgUnitCascadeDialog.CascadeOrgUnitCallbacks() {
                @Override
                public void textChangedConsumer(String selectedOrgUnitUid, String selectedOrgUnitName) {
                    selectedOrgUnit = selectedOrgUnitUid;
                    processor.onNext(RowAction.create(model.uid(), selectedOrgUnitUid));
                    editText.setText(selectedOrgUnitName);
                    instance.dismiss();
                    editText.setEnabled(true);
                }

                @Override
                public void onDialogCancelled() {
                    editText.setEnabled(true);
                }
            });

        return instance;
    }

    public OrgUnitCascadeDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public OrgUnitCascadeDialog setCallbacks(CascadeOrgUnitCallbacks callbacks) {
        this.callbacks = callbacks;
        return this;
    }

    public static void setSelectedOrgUnit(String orgUnitUid){
        selectedOrgUnit = orgUnitUid;
    }

    public OrgUnitCascadeDialog setLevels(List<OrganisationUnitLevel> levels){
        this.levels = levels;
        return this;
    }

    public OrgUnitCascadeDialog setOrgUnits(List<OrganisationUnitModel> orgUnits) {
        this.orgUnits = new ArrayList<>();
        this.paths = new HashMap<>();
        List<String> orgUnitsUid = new ArrayList<>();

        if (orgUnits != null) {
            for (OrganisationUnitModel orgUnit : orgUnits) { //Users OrgUnits
                this.orgUnits.add(Quintet.create(orgUnit.uid(),
                        orgUnit.displayName(),
                        orgUnit.parent() != null ? orgUnit.parent() : "",
                        orgUnit.level(),
                        true));//OrgUnit Uid, OrgUnit Name, Parent Uid, Level, CanBeSelected
                orgUnitsUid.add(orgUnit.uid());
            }

            for (OrganisationUnitModel orgUnit : orgUnits) { //Path OrgUnits
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
        }
        return this;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_cascade_orgunit, container, false);
        binding.orgUnitEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0);
        binding.orgUnitEditText.setHint(title);
        binding.acceptButton.setOnClickListener(view -> {
            if (binding.recycler.getAdapter() != null) {
                binding.orgUnitEditText.getText().clear();
                showChips(new ArrayList<>());
                String selectedOrgUnitUid = ((OrgUnitCascadeAdapter) binding.recycler.getAdapter()).getSelectedOrgUnit();
                for (Quintet<String, String, String, Integer, Boolean> orgUnit : orgUnits) {
                    if (orgUnit.val0().equals(selectedOrgUnitUid) && orgUnit.val4()) {
                        callbacks.textChangedConsumer(orgUnit.val0(), orgUnit.val1());
                    }
                }
            }
        });
        binding.cancelButton.setOnClickListener(view -> {
            callbacks.onDialogCancelled();
            dismiss();
        });
        binding.clearButton.setOnClickListener(view -> {
            binding.orgUnitEditText.getText().clear();
            showChips(new ArrayList<>());
            adapter = new OrgUnitCascadeAdapter(orgUnits, canBeSelected -> {
                if (canBeSelected) {
                    binding.acceptButton.setVisibility(View.VISIBLE);
                } else {
                    binding.acceptButton.setVisibility(View.INVISIBLE);
                }
            }, levels, paths.size() == 1 );
            binding.recycler.setAdapter(adapter);
            binding.acceptButton.setVisibility(View.INVISIBLE);
        });

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

        adapter = new OrgUnitCascadeAdapter(orgUnits, canBeSelected -> {
            if (canBeSelected) {
                binding.acceptButton.setVisibility(View.VISIBLE);
            } else {
                binding.acceptButton.setVisibility(View.INVISIBLE);
            }
        }, levels, paths.size() == 1 );

        if (selectedOrgUnit != null){
            for (Quintet<String, String, String, Integer, Boolean> orgUnit : orgUnits){
                if (orgUnit.val0().equals(selectedOrgUnit)){
                    adapter.setOrgUnit(orgUnit,paths.get(orgUnit.val0()));
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
        binding.recycler.setAdapter(adapter);

        return binding.getRoot();
    }

    private void showChips(ArrayList<Quintet<String, String, String, Integer, Boolean>> data) {
        binding.results.removeAllViews();
        for (Quintet<String, String, String, Integer, Boolean> trio : data) {
            if (trio.val4() && getContext() != null) { //Only shows selectable orgUnits
                Chip chip = new Chip(getContext());
                String level = "";
                for(OrganisationUnitLevel orgLevel: levels){
                    if(trio.val3().intValue() == orgLevel.level()){
                        level = orgLevel.displayName() + " : ";
                    }
                }
                if(level.isEmpty())
                    level = "Lvl. " + trio.val3() + " : ";

                chip.setText(level + "" + trio.val1());
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
        binding.orgUnitEditText.getText().clear();
        disposable.clear();
        super.dismiss();
    }
}
