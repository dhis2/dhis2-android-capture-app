package org.dhis2.data.forms.dataentry.tablefields.spinner;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.Constants;
import org.dhis2.utils.custom_views.OptionSetCellDialog;
import org.dhis2.utils.custom_views.OptionSetCellPopUp;
import org.dhis2.utils.custom_views.OptionSetDialog;
import org.dhis2.utils.custom_views.OptionSetOnClickListener;
import org.hisp.dhis.android.core.option.OptionModel;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 07/11/2017.
 */

public class SpinnerHolder extends FormViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, OptionSetOnClickListener {

    private final FlowableProcessor<RowAction> processor;
    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;
    private final ImageView iconView;
    private final TextView editText;
    private final TextInputLayout inputLayout;

    private SpinnerViewModel viewModel;
    private int numberOfOptions = 0;

    SpinnerHolder(ViewDataBinding mBinding, FlowableProcessor<RowAction> processor, FlowableProcessor<Trio<String, String, Integer>> processorOptionSet) {
        super(mBinding);
        this.editText = mBinding.getRoot().findViewById(R.id.inputEditText);
        this.iconView = mBinding.getRoot().findViewById(R.id.renderImage);
        this.inputLayout = mBinding.getRoot().findViewById(R.id.input_layout);
        this.processor = processor;
        this.processorOptionSet = processorOptionSet;

        editText.setOnClickListener(this);

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                editText.performClick();
        });


    }

    public void update(SpinnerViewModel viewModel, boolean accessDataWrite) {
        this.viewModel = viewModel;

        editText.setText(viewModel.value()); //option code is already transformed to value in the fieldviewmodelfactory implementation

        if (!viewModel.editable()) {
            editText.setEnabled(false);
        } else if(accessDataWrite) {
            editText.setEnabled(true);
        }else{
            editText.setEnabled(false);
        }
    }

    public void dispose() {
    }

    @Override
    public void onClick(View v) {
        if (numberOfOptions > itemView.getContext().getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE).getInt(Constants.OPTION_SET_DIALOG_THRESHOLD, 15)) {
            OptionSetCellDialog dialog = OptionSetCellDialog.newInstance();
            dialog
                    .setProcessor(processorOptionSet)
                    .setOptionSetUid(viewModel)
                    .setOnClick(this)
                    .setCancelListener(view -> dialog.dismiss())
                    .setClearListener(view -> {
                                processor.onNext(
                                        RowAction.create(viewModel.uid(),viewModel.value(), viewModel.dataElement(),
                                                viewModel.listCategoryOption(), viewModel.catCombo(), viewModel.row(), viewModel.column() ));
                                viewModel.withValue(null);
                                dialog.dismiss();
                            }
                    ).show(((FragmentActivity) binding.getRoot().getContext()).getSupportFragmentManager(), null);
        } else {
            OptionSetCellPopUp.getInstance()
                    .setOptionSetUid(viewModel)
                    .setProcessor(processorOptionSet)
                    .setOnClick(this)
                    .show(itemView.getContext(), v);
        }
    }

    @Override
    public void onSelectOption(OptionModel option) {
        setValueOption(option.displayName(), option.code());
        OptionSetDialog.newInstance().dismiss();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (OptionSetCellPopUp.getInstance().getOptions() != null && item.getTitle() != null) {
            OptionModel selectedOption = OptionSetCellPopUp.getInstance().getOptions().get(item.getTitle().toString());
            if (selectedOption != null) {
                setValueOption(selectedOption.displayName(), selectedOption.code());
            }
            OptionSetCellPopUp.getInstance().dismiss();
        }
        return false;
    }

    private void setValueOption(String optionDisplayName, String optionCode) {

        editText.setText(optionDisplayName);
        processor.onNext(
                RowAction.create(viewModel.uid(),optionCode, viewModel.dataElement(),
                        viewModel.listCategoryOption(), viewModel.catCombo(), viewModel.row(), viewModel.column())
        );

    }
}
