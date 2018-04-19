package com.dhis2.usescases.searchTrackEntity.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchTrackedEntityBinding;
import com.dhis2.databinding.TrackEntityProgramsBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.utils.OnErrorHandler;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

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
                        .flatMap(data -> {
                            setEnrollment(data);
                            return metadataRepository.getTEIProgramsToEnroll(trackedEntityInstanceModel.trackedEntityType());
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::setPopUp, OnErrorHandler.create())
        );

        //endregion


        //--------------------------
        //region ATTRI
        if (presenter.getProgramModel() == null)
            compositeDisposable.add(
                    metadataRepository.getTEIAttributeValues(trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setTEIData, OnErrorHandler.create())

            );
        else
            compositeDisposable.add(
                    metadataRepository.getTEIAttributeValues(presenter.getProgramModel().uid(), trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setTEIData, OnErrorHandler.create())

            );
        //endregion

        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onTEIClick(trackedEntityInstanceModel.uid()));
    }

    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }

    private void setPopUp(List<ProgramModel> programModels) {

        List<ProgramModel> possibleEnrollmentPrograms = new ArrayList<>();

        boolean found;
        boolean active;
        for (ProgramModel programModel : programModels) {
            found = false;
            active = false;
            for (EnrollmentModel enrollment : teiEnrollments) {
                if (programModel.uid().equals(enrollment.program())) {
                    found = true;
                    active = enrollment.enrollmentStatus() == EnrollmentStatus.ACTIVE;
                }
            }

            if (found) {//TODO: ENROLLMENT STATUS
                if (!active && !programModel.onlyEnrollOnce())
                    possibleEnrollmentPrograms.add(programModel);
            } else
                possibleEnrollmentPrograms.add(programModel);

        }

        menu = new PopupMenu(binding.addProgram.getContext(), binding.addProgram);
        for (ProgramModel program : possibleEnrollmentPrograms) {
            menu.getMenu().add(Menu.NONE, Menu.NONE, Menu.NONE, program.displayShortName());
        }
        menu.setOnMenuItemClickListener(item -> {
            for (ProgramModel programModel : programModels) {
                if (programModel.displayShortName().equals(item.getTitle())) {
                    presenter.enroll(programModel.uid(), tei.uid());
                    return true;
                }
            }
            return true;
        });

        binding.addProgram.setOnClickListener(view -> menu.show());
    }

    private void setEnrollment(List<EnrollmentModel> enrollments) {
        this.teiEnrollments = enrollments;
        binding.linearLayout.removeAllViews();
        boolean isFollowUp = false;
        for (EnrollmentModel enrollment : enrollments) {
            if (enrollment.enrollmentStatus() == EnrollmentStatus.ACTIVE) {
                TrackEntityProgramsBinding programsBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(binding.linearLayout.getContext()), R.layout.track_entity_programs, binding.linearLayout, false
                );
                programsBinding.setEnrollment(enrollment);
                programsBinding.executePendingBindings();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                binding.linearLayout.addView(programsBinding.getRoot(), layoutParams);
                binding.linearLayout.invalidate();
            }

            if (enrollment.followUp() != null && enrollment.followUp())
                isFollowUp = true;

            binding.setFollowUp(isFollowUp);

        }
        binding.viewMore.setVisibility(binding.linearLayout.getChildCount() > 2 ? View.VISIBLE : View.GONE);

        binding.executePendingBindings();
    }
}
