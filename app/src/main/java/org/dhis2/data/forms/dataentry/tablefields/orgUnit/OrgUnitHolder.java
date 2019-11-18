package org.dhis2.data.forms.dataentry.tablefields.orgUnit;

import android.view.View;
import android.widget.ImageView;

import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.utils.customviews.OrgUnitDialog;
import org.dhis2.utils.customviews.TextInputAutoCompleteTextView;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */
//TODO: CHECK IF THIS IS BEING USED IN DATASETS
public class OrgUnitHolder extends FormViewHolder {
    private final TextInputAutoCompleteTextView editText;
    private final TextInputLayout inputLayout;
    private final ImageView description;
    private OrgUnitDialog orgUnitDialog;
    private CompositeDisposable compositeDisposable;
    private OrgUnitViewModel model;

    OrgUnitHolder(FragmentManager fm, ViewDataBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        compositeDisposable = new CompositeDisposable();
        this.editText = binding.getRoot().findViewById(R.id.input_editText);
        this.inputLayout = binding.getRoot().findViewById(R.id.input_layout);
        this.description = binding.getRoot().findViewById(R.id.descriptionLabel);

        this.editText.setOnClickListener(view -> {
            editText.setEnabled(false);
            orgUnitDialog = new OrgUnitDialog()
                    .setTitle(model.label())
                    .setMultiSelection(false)
                    .setPossitiveListener(data -> {
                        processor.onNext(RowAction.create(model.uid(), orgUnitDialog.getSelectedOrgUnit(), model.dataElement(), model.categoryOptionCombo(),model.catCombo(), model.row(), model.column()));
                        this.editText.setText(orgUnitDialog.getSelectedOrgUnitName());
                        orgUnitDialog.dismiss();
                        editText.setEnabled(true);
                    })
                    .setNegativeListener(data -> {
                        orgUnitDialog.dismiss();
                        editText.setEnabled(true);
                    });
            if (!orgUnitDialog.isAdded())
                orgUnitDialog.show(fm, model.label());
        });
    }

    @Override
    public void dispose() {
        compositeDisposable.clear();
    }

    public void update(OrgUnitViewModel viewModel) {

        descriptionText = viewModel.description();
        label = new StringBuilder(viewModel.label());
        if (viewModel.mandatory())
            label.append("*");
        this.inputLayout.setHint(label.toString());

        if (label.length() > 16 || viewModel.description() != null)
            description.setVisibility(View.VISIBLE);
        else
            description.setVisibility(View.GONE);

        if (viewModel.warning() != null)
            editText.setError(viewModel.warning());
        else if (viewModel.error() != null)
            editText.setError(viewModel.error());
        else
            editText.setError(null);

        if (viewModel.value() != null) {
            editText.post(() -> editText.setText(getOrgUnitName(viewModel.value())));
        }
        editText.setEnabled(false);

        this.model = viewModel;

    }

    private String getOrgUnitName(String value) {
      return value;
    }
}
