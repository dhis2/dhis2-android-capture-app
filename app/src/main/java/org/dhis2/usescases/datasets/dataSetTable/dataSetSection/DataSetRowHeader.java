package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.view.View;

import androidx.databinding.ObservableField;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.dhis2.R;
import org.dhis2.databinding.ItemDatasetRowBinding;
import org.dhis2.utils.Constants;
import org.dhis2.utils.custom_views.CustomDialog;
import org.hisp.dhis.android.core.dataelement.DataElement;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetRowHeader extends AbstractViewHolder {

    public ItemDatasetRowBinding binding;

    DataSetRowHeader(ItemDatasetRowBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(DataElement dataElement, ObservableField<DataSetTableAdapter.TableScale> currentTableScale, Boolean dataElementDecoration) {
        binding.setTableScale(currentTableScale);
        binding.title.setText(!isEmpty(dataElement.displayFormName()) ? dataElement.displayFormName() : dataElement.displayName());

        if (binding.title.getText().length() > 16 || dataElementDecoration) {
            binding.descriptionLabel.setVisibility(View.VISIBLE);
            binding.descriptionLabel.setOnClickListener(v ->
                    new CustomDialog(
                            itemView.getContext(),
                            dataElement.displayName(),
                            dataElement.displayDescription() != null ? dataElement.displayDescription() : itemView.getContext().getString(R.string.empty_description),
                            itemView.getContext().getString(R.string.action_accept),
                            null,
                            Constants.DESCRIPTION_DIALOG,
                            null
                    ).show());
        } else
            binding.descriptionLabel.setVisibility(View.GONE);
    }
}
