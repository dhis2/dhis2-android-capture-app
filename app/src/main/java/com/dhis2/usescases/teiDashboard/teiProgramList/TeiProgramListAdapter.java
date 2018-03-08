package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class TeiProgramListAdapter extends RecyclerView.Adapter<TeiProgramListEnrollmentViewHolder> {

    private TeiProgramListContract.Presenter presenter;
    private List<TeiProgramListItem> listItems;
    private List<EnrollmentModel> activeEnrollments;
    private List<EnrollmentModel> inactiveEnrollments;

    TeiProgramListAdapter(TeiProgramListContract.Presenter presenter) {
        this.presenter = presenter;
        this.listItems = new ArrayList<>();
        this.activeEnrollments = new ArrayList<>();
        this.inactiveEnrollments = new ArrayList<>();
    }

    @Override
    public TeiProgramListEnrollmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding;

        switch (viewType){
            case TeiProgramListItem.TeiProgramListItemViewType.FIRST_TITLE:
                binding = DataBindingUtil.inflate(inflater, R.layout.item_tei_programs_active_title, parent, false);
                break;
            case TeiProgramListItem.TeiProgramListItemViewType.ACTIVE_ENROLLMENT:
                binding = DataBindingUtil.inflate(inflater, R.layout.item_tei_programs_enrollment, parent, false);
                break;
            case TeiProgramListItem.TeiProgramListItemViewType.PROGRAM:
                // TODO CRIS
                binding = DataBindingUtil.inflate(inflater, R.layout.item_tei_programs_enrollment, parent, false);
                break;
            case TeiProgramListItem.TeiProgramListItemViewType.SECOND_TITLE:
                binding = DataBindingUtil.inflate(inflater, R.layout.item_tei_programs_inactive_title, parent, false);
                break;
            case TeiProgramListItem.TeiProgramListItemViewType.INACTIVE_ENROLLMENT:
                binding = DataBindingUtil.inflate(inflater, R.layout.item_tei_programs_enrollment_inactive, parent, false);
                break;
            default:
                // TODO CRIS
                binding = DataBindingUtil.inflate(inflater, R.layout.item_tei_programs_enrollment, parent, false);
                break;
        }

        return new TeiProgramListEnrollmentViewHolder(binding);
    }

    @Override
    public int getItemViewType(int position) {
        return listItems.get(position).getViewType();
    }

    @Override
    public void onBindViewHolder(TeiProgramListEnrollmentViewHolder holder, int position) {
        switch (listItems.get(position).getViewType()){
            case TeiProgramListItem.TeiProgramListItemViewType.FIRST_TITLE:
                holder.bind(presenter, listItems.get(position).getEnrollmentModel());
                break;
            case TeiProgramListItem.TeiProgramListItemViewType.ACTIVE_ENROLLMENT:
                holder.bind(presenter, listItems.get(position).getEnrollmentModel());
                break;
            case TeiProgramListItem.TeiProgramListItemViewType.PROGRAM:
                break;
            case TeiProgramListItem.TeiProgramListItemViewType.SECOND_TITLE:
                break;
            case TeiProgramListItem.TeiProgramListItemViewType.INACTIVE_ENROLLMENT:
                holder.bind(presenter, listItems.get(position).getEnrollmentModel());
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return listItems != null ? listItems.size() : 0;
    }

    void setActiveEnrollments(List<EnrollmentModel> enrollments){
        this.activeEnrollments.clear();
        this.activeEnrollments.addAll(enrollments);
        orderList();
    }

    void setOtherEnrollments(List<EnrollmentModel> enrollments){
        this.inactiveEnrollments.clear();
        this.inactiveEnrollments.addAll(enrollments);
        orderList();
    }

    private void orderList(){
        this.listItems.clear();

        TeiProgramListItem firstTeiProgramListItem = new TeiProgramListItem(null, null, TeiProgramListItem.TeiProgramListItemViewType.FIRST_TITLE);
        this.listItems.add(firstTeiProgramListItem);

        for (EnrollmentModel enrollmentModel : activeEnrollments){
            TeiProgramListItem teiProgramListItem = new TeiProgramListItem(enrollmentModel, null, TeiProgramListItem.TeiProgramListItemViewType.ACTIVE_ENROLLMENT);
            listItems.add(teiProgramListItem);
        }

        if (!inactiveEnrollments.isEmpty()) {
            TeiProgramListItem secondTeiProgramListItem = new TeiProgramListItem(null, null, TeiProgramListItem.TeiProgramListItemViewType.SECOND_TITLE);
            this.listItems.add(secondTeiProgramListItem);

            for (EnrollmentModel enrollmentModel : inactiveEnrollments) {
                TeiProgramListItem teiProgramListItem = new TeiProgramListItem(enrollmentModel, null, TeiProgramListItem.TeiProgramListItemViewType.INACTIVE_ENROLLMENT);
                listItems.add(teiProgramListItem);
            }
        }

        notifyDataSetChanged();
    }
}
