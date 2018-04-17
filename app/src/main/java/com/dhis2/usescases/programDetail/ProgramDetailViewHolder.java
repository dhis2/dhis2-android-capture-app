package com.dhis2.usescases.programDetail;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;
import com.dhis2.databinding.ItemProgramTrackedEntityBinding;
import com.dhis2.utils.DateUtils;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by frodriguez on 10/31/2017.
 */

public class ProgramDetailViewHolder extends RecyclerView.ViewHolder {

    private ItemProgramTrackedEntityBinding binding;
    private CompositeDisposable disposable;

    ProgramDetailViewHolder(ItemProgramTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        disposable = new CompositeDisposable();
    }

    public void bind(ProgramDetailContractModule.Presenter presenter,
                     ProgramModel program,
                     String orgUnit,
                     List<String> attributes,
                     String stage,
                     String uid,
                     boolean followUp) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.program, program);
        binding.setVariable(BR.orgUnit, orgUnit);
        binding.setVariable(BR.attribute, attributes);
        binding.setVariable(BR.stage, stage);
        binding.setVariable(BR.followUp, followUp);
        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onTEIClick(uid, program.uid()));
    }

    public void bind(ProgramDetailContractModule.Presenter presenter, String program, TrackedEntityInstanceModel trackedEntityInstanceModel, ProgramRepository programRepository) {

        itemView.setOnClickListener(view -> presenter.onTEIClick(trackedEntityInstanceModel.uid(), program));

        binding.setTei(trackedEntityInstanceModel);

        disposable.add(
                programRepository.programAttributesValues(program, trackedEntityInstanceModel.uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                programTrackedEntityAttributeModels -> {
                                    if (programTrackedEntityAttributeModels.size() > 0) {
                                        binding.entityAttribute1.setText(programTrackedEntityAttributeModels.get(0).value());
                                        binding.entityAttribute1.setVisibility(View.VISIBLE);
                                    }
                                    if (programTrackedEntityAttributeModels.size() > 1) {
                                        binding.entityAttribute2.setText(programTrackedEntityAttributeModels.get(1).value());
                                        binding.entityAttribute2.setVisibility(View.VISIBLE);
                                    }
                                    if (programTrackedEntityAttributeModels.size() > 2) {
                                        binding.entityAttribute3.setText(programTrackedEntityAttributeModels.get(2).value());
                                        binding.entityAttribute3.setVisibility(View.VISIBLE);
                                    }
                                },
                                Timber::d
                        )
        );

        disposable.add(
                programRepository.enrollments(program, trackedEntityInstanceModel.uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                enrollments -> { //enrolments ordered by enrolment date desc. should only check most recent
                                    if (enrollments.get(0).followUp())
                                        binding.followUp.setVisibility(View.VISIBLE);
                                    binding.setEnrollment(enrollments.get(0));
                                    binding.enrollmentDate.setText(DateUtils.uiDateFormat().format(enrollments.get(0).dateOfEnrollment()));

                                },
                                Timber::d
                        )
        );

        disposable.add(
                programRepository.programStage(program, trackedEntityInstanceModel.uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                stages -> { //stage ordered by eventDate desc. should only check most recent
                                    binding.setProgramStage(stages.get(0));
                                },
                                Timber::d
                        )
        );


    }
}
