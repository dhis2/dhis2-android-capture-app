package com.dhis2.data.forms;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.section.viewmodels.date.DatePickerDialogFragment;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.utils.Preconditions;
import com.jakewharton.rxbinding2.view.RxView;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;


public class FormFragment extends FragmentGlobalAbstract implements FormView {
    private static final String FORM_VIEW_ARGUMENTS = "formViewArguments";

    View fab;
    ViewPager viewPager;
    TabLayout tabLayout;
    Toolbar toolbar;

    @Inject
    FormPresenter formPresenter;

    private FormSectionAdapter formSectionAdapter;
    private PublishSubject<String> onReportDateChanged;
    private TextView reportDate;
    PublishSubject<ReportStatus> undoObservable;
    private CoordinatorLayout coordinatorLayout;

    public FormFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(@NonNull FormViewArguments formViewArguments) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putParcelable(FORM_VIEW_ARGUMENTS, formViewArguments);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_form, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab = view.findViewById(R.id.fab_complete_event);
        viewPager = view.findViewById(R.id.viewpager_dataentry);
        tabLayout = view.findViewById(R.id.tablayout_data_entry);
        toolbar = view.findViewById(R.id.toolbar);
        reportDate = view.findViewById(R.id.textview_report_date);
        coordinatorLayout = view.findViewById(R.id.coordinatorlayout_form);
        formSectionAdapter = new FormSectionAdapter(getFragmentManager());
        viewPager.setAdapter(formSectionAdapter);
        tabLayout.setupWithViewPager(viewPager);

        setupActionBar();
        initReportDatePicker();
    }

    private void setupActionBar() {
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setHomeButtonEnabled(true);
            }
        }
    }

    @NonNull
    @Override
    public Observable<ReportStatus> eventStatusChanged() {
        undoObservable = PublishSubject.create();
        return undoObservable.mergeWith(RxView.clicks(fab).map(o -> getReportStatusFromFab()));
    }

    @NonNull
    @Override
    public Observable<String> reportDateChanged() {
        onReportDateChanged = PublishSubject.create();
        return onReportDateChanged;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FormViewArguments arguments = Preconditions.isNull(getArguments()
                .getParcelable(FORM_VIEW_ARGUMENTS), "formViewArguments == null");

        ((App) getActivity().getApplicationContext())
                .createFormComponent(new FormModule(arguments))
                .inject(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        ((App) getActivity().getApplicationContext())
                .releaseFormComponent();
    }

    @Override
    public void onResume() {
        super.onResume();
        formPresenter.onAttach(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        formPresenter.onDetach();
    }

    @NonNull
    @Override
    public Consumer<List<FormSectionViewModel>> renderSectionViewModels() {
        return sectionViewModels -> {
            formSectionAdapter.swapData(sectionViewModels);
            if (sectionViewModels.size() == 0) {
                Log.d("EMPTY", "Show empty state");
                // TODO: Show empty state
            }

        };
    }

    @NonNull
    @Override
    public Consumer<String> renderReportDate() {
        return date -> {
            reportDate.setText(date);
        };
    }

    @NonNull
    @Override
    public Consumer<String> renderTitle() {
        return title -> Log.d("d", title);
    }

    @NonNull
    @Override
    public Consumer<ReportStatus> renderStatus() {
        return eventStatus -> fab.setActivated(eventStatus == ReportStatus.COMPLETED);
    }

    @NonNull
    @Override
    public Consumer<String> finishEnrollment() {
        return enrollmentUid -> {
            getActivity().finish();
        };
    }

    @Override
    public void renderStatusChangeSnackBar(@NonNull ReportStatus reportStatus) {
        String snackBarMessage = reportStatus == ReportStatus.COMPLETED ?
                "Completed" : "Activated";

        Snackbar.make(coordinatorLayout, snackBarMessage, Snackbar.LENGTH_LONG)
                .setAction("Revert", v1 -> {
                    if (undoObservable == null) {
                        return;
                    }
                    if (reportStatus == ReportStatus.COMPLETED) {
                        undoObservable.onNext(ReportStatus.ACTIVE);
                    } else {
                        undoObservable.onNext(ReportStatus.COMPLETED);
                    }
                })
                .show();
    }

    private ReportStatus getReportStatusFromFab() {
        return fab.isActivated() ? ReportStatus.ACTIVE : ReportStatus.COMPLETED;
    }

    private void initReportDatePicker() {
        reportDate.setOnClickListener(v -> {
            DatePickerDialogFragment dialog = DatePickerDialogFragment.create(false);
            dialog.show(getFragmentManager());
            dialog.setFormattedOnDateSetListener(publishReportDateChange());
        });
    }

    @NonNull
    private DatePickerDialogFragment.FormattedOnDateSetListener publishReportDateChange() {
        return date -> {
            if (onReportDateChanged != null) {
                onReportDateChanged.onNext(BaseIdentifiableObject.DATE_FORMAT.format(date));
            }
        };
    }
}