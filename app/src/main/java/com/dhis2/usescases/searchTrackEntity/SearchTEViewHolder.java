package com.dhis2.usescases.searchTrackEntity;

import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.widget.Toast;

import com.dhis2.databinding.ItemSearchTrackedEntityBinding;

import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchTEViewHolder extends RecyclerView.ViewHolder {

    ItemSearchTrackedEntityBinding binding;
    List<ProgramModel> programsNotEnrolled;
    List<ProgramModel> programsEnrolled;
    PopupMenu menu;

    public SearchTEViewHolder(ItemSearchTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(SearchTEPresenter presenter,
                     TrackedEntityInstance entityInstance,
                     List<String> attributes,
                     List<TrackedEntityAttributeModel> attributeModels,
                     List<ProgramModel> programModels) {

        binding.setPresenter(presenter);
        binding.setAttribute(attributes);

        programsEnrolled = new ArrayList<>();
        programsNotEnrolled = new ArrayList<>();

        programsNotEnrolled.addAll(programModels);

        if (entityInstance.enrollments() != null) {
            for (Enrollment enrollment : entityInstance.enrollments()) {
                for (ProgramModel programModel : programModels) {
                    if (programModel.uid().equals(enrollment.program()))
                        programsEnrolled.add(programModel);
                }
            }

            programsNotEnrolled.removeAll(programsEnrolled);

        }

        if (!programsEnrolled.isEmpty())
            binding.setProgram(programsEnrolled.get(0));


        menu = new PopupMenu(binding.addProgram.getContext(), binding.addProgram);
        for (ProgramModel program :
                programsNotEnrolled) {
            menu.getMenu().add(Menu.NONE, Menu.NONE, Menu.NONE, program.displayShortName());
        }
        menu.setOnMenuItemClickListener(item -> {
            Toast.makeText(binding.addProgram.getContext(), item.getTitle(), Toast.LENGTH_LONG).show();
            return true;
        });

        binding.addProgram.setOnClickListener(view -> menu.show());

        binding.executePendingBindings();

    }

}
