package com.dhis2.data.forms.dataentry;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.utils.Preconditions;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public final class DataEntryFragment extends FragmentGlobalAbstract implements DataEntryView {
    private static final String ARGUMENTS = "args";

    @Inject
    DataEntryPresenter dataEntryPresenter;

    DataEntryAdapter dataEntryAdapter;

    RecyclerView recyclerView;

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

        DataEntryArguments args = Preconditions.isNull(getArguments()
                .getParcelable(ARGUMENTS), "dataEntryArguments == null");

        ((App) getActivity().getApplicationContext())
                .formComponent()
                .plus(new DataEntryModule(context, args),
                        new DataEntryStoreModule(args))
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_entry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerview_data_entry);
        setUpRecyclerView();
        dataEntryPresenter.onAttach(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dataEntryPresenter.onDetach();
    }

    @NonNull
    @Override
    public Flowable<RowAction> rowActions() {
        return dataEntryAdapter.asFlowable();
    }

    @NonNull
    @Override
    public Consumer<List<FieldViewModel>> showFields() {
        return fields -> dataEntryAdapter.swap(fields);
    }

    private void setUpRecyclerView() {
        dataEntryAdapter = new DataEntryAdapter(LayoutInflater.from(getActivity()),
                getChildFragmentManager(), getArguments().getParcelable(ARGUMENTS));
        dataEntryAdapter.setHasStableIds(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(dataEntryAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL));
    }
}
