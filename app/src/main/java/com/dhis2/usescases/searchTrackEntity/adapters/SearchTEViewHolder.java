package com.dhis2.usescases.searchTrackEntity.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchTrackedEntityBinding;
import com.dhis2.databinding.TrackEntityProgramsBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.google.android.flexbox.FlexboxLayout;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchTEViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchTrackedEntityBinding binding;
    private PopupMenu menu;
    private CompositeDisposable compositeDisposable;
    private List<EnrollmentModel> teiEnrollments;
    private SearchTEContractsModule.Presenter presenter;
    private TrackedEntityInstanceModel tei;

    public SearchTEViewHolder(ItemSearchTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        compositeDisposable = new CompositeDisposable();
    }


    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityInstanceModel trackedEntityInstanceModel, MetadataRepository metadataRepository) {
        this.presenter = presenter;
        binding.setPresenter(presenter);
        this.tei = trackedEntityInstanceModel;
        //--------------------------
        //region ENROLLMENTS

        compositeDisposable.add(
                metadataRepository.getTEIEnrollments(trackedEntityInstanceModel.uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::setEnrollment, Timber::d));
        //endregion


        //--------------------------
        //region ATTRI
        if (presenter.getProgramModel() == null)
            compositeDisposable.add(
                    metadataRepository.getTEIAttributeValues(trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setTEIData, Timber::d)

            );
        else
            compositeDisposable.add(
                    metadataRepository.getTEIAttributeValues(presenter.getProgramModel().uid(), trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setTEIData, Timber::d)

            );
        //endregion

        //--------------------------
        //region OVERDUE EVENTS
        compositeDisposable.add(
                metadataRepository.hasOverdue(presenter.getProgramModel() != null ? presenter.getProgramModel().uid() : null, trackedEntityInstanceModel.uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(hasOverDue -> binding.setOverdue(hasOverDue), Timber::d)
        );
        //endregion

        binding.setSyncState(tei.state());

        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onTEIClick(trackedEntityInstanceModel.uid()));
    }

    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }

    private void setEnrollment(List<EnrollmentModel> enrollments) {
        this.teiEnrollments = enrollments;
        binding.linearLayout.removeAllViews();
        boolean isFollowUp = false;
        for (EnrollmentModel enrollment : enrollments) {
            if (enrollment.enrollmentStatus() == EnrollmentStatus.ACTIVE && binding.linearLayout.getChildCount() < 2) {
                TrackEntityProgramsBinding programsBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(binding.linearLayout.getContext()), R.layout.track_entity_programs, binding.linearLayout, false
                );
                programsBinding.setEnrollment(enrollment);
                programsBinding.executePendingBindings();
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setFlexBasisPercent(.5f);
                binding.linearLayout.addView(programsBinding.getRoot(), params);
                binding.linearLayout.invalidate();
            }

            if (enrollment.followUp() != null && enrollment.followUp())
                isFollowUp = true;

            binding.setFollowUp(isFollowUp);

        }
        binding.viewMore.setVisibility(enrollments.size() > 2 ? View.VISIBLE : View.GONE);

        binding.executePendingBindings();
    }
}
