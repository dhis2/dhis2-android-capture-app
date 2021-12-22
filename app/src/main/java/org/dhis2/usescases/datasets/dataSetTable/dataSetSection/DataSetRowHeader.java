package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import static android.text.TextUtils.isEmpty;

import android.view.View;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableField;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.dhis2.R;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.databinding.ItemDatasetRowBinding;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.dataelement.DataElement;

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

        boolean hasDescription = dataElement.displayDescription() != null && !dataElement.displayDescription().isEmpty();
        boolean showDecoration = dataElementDecoration.equals(true);
        if (showDecoration || hasDescription) {
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

    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);

        if (selectionState == SelectionState.UNSELECTED) {
            setBackground();
        } else {
            int bgColor = itemView.getSolidColor();
            setTextColor(ColorUtils.getContrastColor(bgColor));
        }
    }

    public void setBackground() {
        int bgColor = android.R.color.white;
        itemView.setBackgroundResource(bgColor);
        setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
    }

    public void setTextColor(@ColorInt int color) {
        binding.title.setTextColor(color);
    }
}
