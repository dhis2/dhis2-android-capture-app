package com.dhis2.usescases.searchTrackEntity;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.usescases.searchTrackEntity.formHolders.ButtonFormHolder;
import com.dhis2.usescases.searchTrackEntity.formHolders.FormViewHolder;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 06/11/2017.
 */

public class FormAdapter extends RecyclerView.Adapter<FormViewHolder> {

    private final int EDITTEXT = 0;
    private final int BUTTON = 1;
    private final int CHECKBOX = 2;
    private final int SPINNER = 3;

    SearchTEContractsModule.Presenter presenter;
    List<TrackedEntityAttributeModel> attributeList;

    @Inject
    public FormAdapter(SearchTEContractsModule.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public FormViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case EDITTEXT:
                return null;
            case BUTTON:
                ViewDataBinding bindingButton = DataBindingUtil.inflate(inflater, R.layout.form_button_text, parent, false);
                return new ButtonFormHolder(bindingButton);
            case CHECKBOX:
                return null;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(FormViewHolder holder, int position) {
        ((ButtonFormHolder) holder).bindData(presenter, attributeList.get(position));

        ((ButtonFormHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                presenter.onDateClick(holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return attributeList != null ? attributeList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (attributeList.get(position).optionSet() != null)
            return 4;
        else
            switch (attributeList.get(position).valueType()) {
                case AGE:
                case URL:
                case TEXT:
                case EMAIL:
                case LETTER:
                case NUMBER:
                case INTEGER:
                case USERNAME:
                case LONG_TEXT:
                case PERCENTAGE:
                case PHONE_NUMBER:
                case INTEGER_NEGATIVE:
                case INTEGER_POSITIVE:
                case INTEGER_ZERO_OR_POSITIVE:
                    return EDITTEXT;
                case DATE:
                case TIME:
                case DATETIME:
                case COORDINATE:
                case FILE_RESOURCE:
                    return BUTTON;
                case BOOLEAN:
                    return CHECKBOX;
                case TRUE_ONLY:
                case TRACKER_ASSOCIATE:
                case UNIT_INTERVAL:
                case ORGANISATION_UNIT:
                default:
                    return SPINNER;
            }

    }

    public void setList(List<TrackedEntityAttributeModel> modelList) {
        ArrayList<TrackedEntityAttributeModel> toRemove = new ArrayList<>();
        for (TrackedEntityAttributeModel trackedEntityAttributeModel : modelList) {
            if (trackedEntityAttributeModel.valueType() != ValueType.DATE)
                toRemove.add(trackedEntityAttributeModel);
        }
        modelList.removeAll(toRemove);
        this.attributeList = modelList;
        notifyDataSetChanged();
    }
}
