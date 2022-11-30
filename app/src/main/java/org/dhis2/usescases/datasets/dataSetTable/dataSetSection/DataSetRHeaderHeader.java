package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableField;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.dhis2.R;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.databinding.ItemDatasetHeaderBinding;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetRHeaderHeader extends AbstractViewHolder {

    protected ItemDatasetHeaderBinding binding;
    private int columnPosition;

    DataSetRHeaderHeader(ItemDatasetHeaderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String rowHeaderTitle, ObservableField<DataSetTableAdapter.TableScale> tableScale, int columnPosition) {
        binding.setTableScale(tableScale);
        binding.title.setText(rowHeaderTitle);
        binding.title.setSelected(true);
        this.columnPosition = columnPosition;
        setBackground(columnPosition % 2 == 0);
    }

    public void setBackground(boolean isEven) {
        int bgColor = isEven ? R.color.even_header_color : R.color.odd_header_color;
        itemView.findViewById(R.id.container).setBackgroundResource(bgColor);
        setTextColorResource(R.color.textSecondary);
    }

    public void setTextColorResource(@ColorRes int textColor) {
        binding.title.setTextColor(ContextCompat.getColor(binding.title.getContext(), textColor));
    }

    public void setTextColor(@ColorInt int color){
        binding.title.setTextColor(color);
    }

    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);

        if (selectionState == SelectionState.UNSELECTED) {
            setBackground(columnPosition % 2 == 0);
        } else {
            int bgColor = binding.container.getSolidColor();
            setTextColor(ColorUtils.getContrastColor(bgColor));
        }
    }
}
