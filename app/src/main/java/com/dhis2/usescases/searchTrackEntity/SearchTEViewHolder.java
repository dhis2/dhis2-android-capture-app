package com.dhis2.usescases.searchTrackEntity;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.dhis2.BR;
import com.dhis2.R;
import com.dhis2.databinding.ItemSearchTrackedEntityBinding;
import com.dhis2.databinding.TrackEntityProgramsBinding;
import com.dhis2.usescases.main.program.HomeViewModel;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchTEViewHolder extends RecyclerView.ViewHolder {

    ItemSearchTrackedEntityBinding binding;
    List<String> programsNotEnrolled = new ArrayList<>();
    List<String> programsEnrolled = new ArrayList<>();

    public SearchTEViewHolder(ItemSearchTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        programsNotEnrolled.add("Program 1");
        programsNotEnrolled.add("Program 2");
        programsNotEnrolled.add("Program 3");
        programsNotEnrolled.add("Program 4");

    }

    public void bind(SearchTEPresenter presenter,
                     TrackedEntityInstance entityInstance,
                     List<String> attributes) {
        binding.setPresenter(presenter);
        binding.setAttribute(attributes);
        binding.executePendingBindings();


        LayoutInflater inflater = LayoutInflater.from(binding.linearLayout.getContext());

        RelativeLayout relativeLayout = null;

      /*  for (String program : programsEnrolled) {
            relativeLayout = new RelativeLayout(binding.getRoot().getContext());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            TrackEntityProgramsBinding viewBinding = DataBindingUtil.inflate(inflater, R.layout.track_entity_programs, binding.linearLayout, false);
            viewBinding.setVariable(BR.programString, program);
            viewBinding.executePendingBindings();
            binding.linearLayout.addView(relativeLayout, layoutParams);
        }*/

       /* TableRow tableRow = null;
        for (int i = 0; i < programsEnrolled.size(); i++) {
            if (i % 2 == 0) {
                tableRow = new TableRow(binding.tableLayout.getContext());
            }
            TrackEntityProgramsBinding viewBinding = DataBindingUtil.inflate(inflater, R.layout.track_entity_programs, binding.tableLayout, false);
            viewBinding.setVariable(BR.programString, programsEnrolled.get(i));
            viewBinding.executePendingBindings();
            tableRow.addView(viewBinding.getRoot());

            if(i % 2 != 0 || i == programsEnrolled.size()-1)
                binding.tableLayout.addView(tableRow,new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        }*/


        binding.addProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(view.getContext(), binding.addProgram);
                for (String program :
                        programsNotEnrolled) {
                    menu.getMenu().add(Menu.NONE, Menu.NONE, Menu.NONE, program);
                }
                menu.show();

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(view.getContext(), item.getTitle(), Toast.LENGTH_LONG).show();
                        return true;
                    }
                });
            }
        });
    }

}
