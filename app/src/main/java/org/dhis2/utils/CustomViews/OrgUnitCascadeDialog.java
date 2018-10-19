package org.dhis2.utils.CustomViews;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.R;
import org.dhis2.databinding.DialogCascadeOrgunitBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class OrgUnitCascadeDialog extends DialogFragment {
    DialogCascadeOrgunitBinding binding;

    private View.OnClickListener possitiveListener;
    private View.OnClickListener negativeListener;
    private String title;
    private CascadeOrgUnitCallbacks callbacks;
    private CompositeDisposable disposable;
    private Observable<List<OrganisationUnitModel>> orgUnitObservable;
    private List<OrganisationUnitModel> orgUnits;


    public OrgUnitCascadeDialog() {
        possitiveListener = null;
        negativeListener = null;
    }

    public OrgUnitCascadeDialog setPossitiveListener(View.OnClickListener listener) {
        this.possitiveListener = listener;
        return this;
    }

    public OrgUnitCascadeDialog setNegativeListener(View.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }

    public OrgUnitCascadeDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public OrgUnitCascadeDialog setCallbacks(CascadeOrgUnitCallbacks callbacks) {
        this.callbacks = callbacks;
        return this;
    }

    public OrgUnitCascadeDialog setOrgUnitObservable(Observable<List<OrganisationUnitModel>> orgUnitObservable) {
        this.orgUnitObservable = orgUnitObservable;
        return this;
    }

    public OrgUnitCascadeDialog setOrgUnitObservable(List<OrganisationUnitModel> orgUnits) {
        this.orgUnits = orgUnits;
        return this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_cascade_orgunit, container, false);

        binding.orgUnitEditText.setHint(title);
        binding.acceptButton.setOnClickListener(possitiveListener);
        binding.cancelButton.setOnClickListener(negativeListener);
        binding.clearButton.setOnClickListener(view -> binding.orgUnitEditText.getText().clear());

        disposable = new CompositeDisposable();

        disposable.add(RxTextView.textChanges(binding.orgUnitEditText)
                .skipInitialValue()
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(data -> data != null)
                .subscribe(
                        data -> Log.d("TEXT_CHANGED", data.toString()),
                        Timber::e
                ));

        if (orgUnitObservable != null)
            disposable.add(orgUnitObservable
                    .subscribe(
                            this.setLevelList(),
                            Timber::e));

        return binding.getRoot();
    }

    private Consumer<List<OrganisationUnitModel>> setLevelList() {
        return data -> {
            Log.d(this.getClass().getSimpleName(), "data ok");
            binding.recycler.setText(data.size() + "orgunits found");
        };
    }

    public interface CascadeOrgUnitCallbacks {
        Consumer<CharSequence> textChangedConsumer();
    }

    @Override
    public void dismiss() {
        disposable.clear();
        super.dismiss();
    }
}
