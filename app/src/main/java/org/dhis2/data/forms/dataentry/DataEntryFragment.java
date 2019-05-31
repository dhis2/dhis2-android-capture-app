package org.dhis2.data.forms.dataentry;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.FormFragment;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.Preconditions;
import org.dhis2.utils.custom_views.OptionSetDialog;
import org.dhis2.utils.custom_views.OptionSetPopUp;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

public final class DataEntryFragment extends FragmentGlobalAbstract implements DataEntryView {
    private static final String ARGUMENTS = "args";

    @Inject
    DataEntryPresenter dataEntryPresenter;

    private DataEntryAdapter dataEntryAdapter;
    private RecyclerView recyclerView;
    private Fragment formFragment;
    private String section;
    private ProgressBar progressBar;
    private View dummyFocusView;
    private boolean isEnrollment;

    @NonNull
    public static DataEntryFragment create(@NonNull DataEntryArguments arguments) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGUMENTS, arguments);

        DataEntryFragment dataEntryFragment = new DataEntryFragment();
        dataEntryFragment.setArguments(bundle);

        return dataEntryFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        formFragment = ((ActivityGlobalAbstract) context).getSupportFragmentManager().getFragments().get(0);
        DataEntryArguments args = Preconditions.isNull(getArguments()
                .getParcelable(ARGUMENTS), "dataEntryArguments == null");

        this.section = args.section();
        this.isEnrollment = args.enrollment()!=null;
        if (((App) context.getApplicationContext()).formComponent() != null)
            ((App) context.getApplicationContext())
                    .formComponent()
                    .plus(new DataEntryModule(context, args), new DataEntryStoreModule(args))
                    .inject(this);
    }

    public String getSection() {
        return section;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_entry, container, false);
        progressBar = view.findViewById(R.id.progress);
        dummyFocusView = view.findViewById(R.id.dummyFocusView);
        Bindings.setProgressColor(progressBar, R.color.colorPrimary);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerview_data_entry);
        setUpRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        dataEntryPresenter.onAttach(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        dataEntryPresenter.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Flowable<RowAction> rowActions() {
        return dataEntryAdapter.asFlowable();
    }

    @NonNull
    @Override
    public Flowable<Trio<String, String, Integer>> optionSetActions() {
        return dataEntryAdapter.asFlowableOption();
    }

    @Override
    public FlowableProcessor<RowAction> getActionProcessor() {
        return dataEntryAdapter.asFlowable();
    }

    @NonNull
    @Override
    public Consumer<List<FieldViewModel>> showFields() {
        return updates -> {
            progressBar.setVisibility(View.INVISIBLE);
            if (!isEmpty(dataEntryPresenter.getLastFocusItem()))
                dataEntryAdapter.setLastFocusItem(dataEntryPresenter.getLastFocusItem());

            if(isEnrollment){
               Iterator<FieldViewModel> iterator = updates.iterator();
               while (iterator.hasNext()){
                   if(iterator.next() instanceof DisplayViewModel)
                       iterator.remove();
               }
            }

            dataEntryAdapter.swap(updates);

        };
    }

    @Override
    public void nextFocus() {
        if (!isEmpty(dataEntryPresenter.getLastFocusItem()))
            dataEntryAdapter.setLastFocusItem(dataEntryPresenter.getLastFocusItem());
        dataEntryAdapter.swapWithoutList();
    }

    @Override
    public void removeSection(String sectionUid) {
        if (formFragment instanceof FormFragment) {
            ((FormFragment) formFragment).hideSections(sectionUid);
        }
    }

    @Override
    public void messageOnComplete(String message, boolean canComplete) {
        //TODO: When event/enrollment ends if there is a message it should be shown. Only if canComplete, user can finish
    }

    public boolean checkMandatory() {
        return dataEntryAdapter.mandatoryOk();
    }

    private void setUpRecyclerView() {
        DataEntryArguments arguments = getArguments().getParcelable(ARGUMENTS);
        dataEntryAdapter = new DataEntryAdapter(LayoutInflater.from(getActivity()),
                getChildFragmentManager(), arguments);

        RecyclerView.LayoutManager layoutManager;
        if (arguments.renderType() != null && arguments.renderType().equals(ProgramStageSectionRenderingType.MATRIX.name())) {
            layoutManager = new GridLayoutManager(getActivity(), 2);
        } else
            layoutManager = new LinearLayoutManager(getActivity(),
                    RecyclerView.VERTICAL, false);
        recyclerView.setAdapter(dataEntryAdapter);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    dataEntryAdapter.setLastFocusItem(null);
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(recyclerView.getWindowToken(), 0);
                    dummyFocusView.requestFocus();
                    dataEntryPresenter.clearLastFocusItem();
                }
            }
        });

    }

    public boolean checkErrors() {
        return dataEntryAdapter.hasError();
    }

    public String getErrorFields() {
        return dataEntryAdapter.getErrorFieldNames();
    }

    @Override
    public void setListOptions(List<OptionModel> options) {
        if (OptionSetDialog.isCreated())
            OptionSetDialog.newInstance().setOptions(options);
        else if (OptionSetPopUp.isCreated())
            OptionSetPopUp.getInstance().setOptions(options);
    }

    @Override
    public void showMessage(int messageId) {
        AlertDialog dialog = showInfoDialog(getString(R.string.error), getString(R.string.unique_warning), new OnDialogClickListener() {

            @Override
            public void onPossitiveClick(androidx.appcompat.app.AlertDialog alertDialog) {
                //nothing
            }

            @Override
            public void onNegativeClick(androidx.appcompat.app.AlertDialog alertDialog) {
                //nothing
            }
        });
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void updateAdapter(RowAction rowAction) {
        getActivity().runOnUiThread(() -> {
            dataEntryAdapter.notifyChanges(rowAction);
            if (rowAction.lastFocusPosition() != -1)
                if (rowAction.lastFocusPosition() >= dataEntryAdapter.getItemCount())
                    recyclerView.smoothScrollToPosition(rowAction.lastFocusPosition());
                else
                    recyclerView.smoothScrollToPosition(rowAction.lastFocusPosition() + 1);
        });

    }
}
