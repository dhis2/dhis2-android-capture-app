package com.dhis2.data.forms.dataentry.fields.old_fields.text;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.hisp.dhis.android.dataentry.R;

import butterknife.BindView;
import butterknife.ButterKnife;

final class TextViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.textview_row_label)
    TextView label;

    @BindView(R.id.textview_row_textview)
    TextView value;

    TextViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    void update(@NonNull TextViewModel viewModel) {
        label.setText(viewModel.label());
        value.setText(viewModel.value());
    }
}
