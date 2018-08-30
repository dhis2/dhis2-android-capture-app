package com.dhis2.usescases.searchTrackEntity.adapters;

import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemSearchTrackedEntityBinding;
import com.dhis2.databinding.TrackEntityProgramsBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.google.android.flexbox.FlexboxLayout;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchTEViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchTrackedEntityBinding binding;
    private CompositeDisposable compositeDisposable;

    SearchTEViewHolder(ItemSearchTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        compositeDisposable = new CompositeDisposable();
    }


    public void bind(SearchTEContractsModule.Presenter presenter, SearchTeiModel searchTeiModel) {
        binding.setPresenter(presenter);
        binding.setOverdue(searchTeiModel.isHasOverdue());
        binding.setIsOnline(searchTeiModel.isOnline());
        binding.setSyncState(searchTeiModel.getTei().state());

        setEnrollment(searchTeiModel.getEnrollments());
        setTEIData(searchTeiModel.getAttributeValues());

        binding.trackedEntityImage.setBackground(ContextCompat.getDrawable(itemView.getContext(),R.drawable.photo_temp_gray));
        binding.followUp.setBackground(ContextCompat.getDrawable(itemView.getContext(),R.drawable.ic_circle_red));

        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onTEIClick(searchTeiModel.getTei().uid(), searchTeiModel.isOnline()));

    }

    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }

    private void setEnrollment(List<EnrollmentModel> enrollments) {
        binding.linearLayout.removeAllViews();
        boolean isFollowUp = false;
        for (EnrollmentModel enrollment : enrollments) {
            if (binding.getPresenter().getProgramModel() != null && enrollment.enrollmentStatus() == EnrollmentStatus.ACTIVE && binding.linearLayout.getChildCount() < 2 &&
                 !binding.getPresenter().getProgramModel().uid().equals(enrollment.program())){
                TrackEntityProgramsBinding programsBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(binding.linearLayout.getContext()), R.layout.track_entity_programs, binding.linearLayout, false
                );

                    programsBinding.setEnrollment(enrollment);

                programsBinding.executePendingBindings();
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                binding.linearLayout.addView(programsBinding.getRoot(), params);
                binding.linearLayout.invalidate();
            }

            if (binding.getPresenter().getProgramModel() != null && enrollment.followUp() != null && enrollment.followUp() && binding.getPresenter().getProgramModel().uid().equals(enrollment.program()))
                isFollowUp = true;
        }

        binding.setFollowUp(isFollowUp);
        binding.viewMore.setVisibility(enrollments.size() > 2 ? View.VISIBLE : View.GONE);
    }

}
