package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.view.View;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.dhis2.R;
import org.dhis2.databinding.ItemDatasetRowBinding;
import org.dhis2.utils.Constants;
import org.dhis2.utils.CustomViews.CustomDialog;
import org.hisp.dhis.android.core.dataelement.DataElementModel;

import java.util.Objects;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetRowHeader extends AbstractViewHolder {

    public ItemDatasetRowBinding binding;

    DataSetRowHeader(ItemDatasetRowBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(DataElementModel dataElement) {
        binding.title.setText(dataElement.displayName());

        if (dataElement.description() != null && !dataElement.description().equals("")) {
            binding.descriptionLabel.setVisibility(View.VISIBLE);
            binding.descriptionLabel.setOnClickListener(v ->
                    new CustomDialog(
                            itemView.getContext(),
                            dataElement.displayName(),
                            Objects.requireNonNull(dataElement.description() != null ? dataElement.description() : "No info for this field"),
                            itemView.getContext().getString(R.string.action_accept),
                            null,
                            Constants.DESCRIPTION_DIALOG,
                            null
                    ).show());
        }else
            binding.descriptionLabel.setVisibility(View.GONE);
    }
}
