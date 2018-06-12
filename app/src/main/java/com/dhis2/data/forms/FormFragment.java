package com.dhis2.data.forms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.dataentry.DataEntryFragment;
import com.dhis2.data.forms.section.viewmodels.date.DatePickerDialogFragment;
import com.dhis2.data.tuples.Pair;
import com.dhis2.data.tuples.Trio;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.map.MapSelectorActivity;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.dhis2.utils.Constants;
import com.dhis2.utils.CustomViews.CoordinatesView;
import com.dhis2.utils.Preconditions;
import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.rxbinding2.view.RxView;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;


public class FormFragment extends FragmentGlobalAbstract implements FormView, CoordinatesView.OnMapPositionClick, CoordinatesView.OnCurrentLocationClick {
    private static final String FORM_VIEW_ARGUMENTS = "formViewArguments";
    private static final String FORM_VIEW_ACTIONBAR = "formViewActionbar";
    private static final String IS_ENROLLMENT = "isEnrollment";

    View nextButton;
    ViewPager viewPager;
    TabLayout tabLayout;
    Toolbar toolbar;

    @Inject
    FormPresenter formPresenter;

    private FormSectionAdapter formSectionAdapter;
    private PublishSubject<String> onReportDateChanged;
    private PublishSubject<String> onIncidentDateChanged;
    private PublishSubject<LatLng> onCoordinatesChanged;
    private TextInputEditText reportDate;
    PublishSubject<ReportStatus> undoObservable;
    private CoordinatorLayout coordinatorLayout;
    private TextInputLayout incidentDateLayout;
    private TextInputEditText incidentDate;
    private CoordinatesView coordinatesView;
    private boolean isEnrollment;

    public FormFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(@NonNull FormViewArguments formViewArguments, boolean isEnrollment, boolean showActionBar) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putParcelable(FORM_VIEW_ARGUMENTS, formViewArguments);
        args.putBoolean(IS_ENROLLMENT, isEnrollment);
        args.putBoolean(FORM_VIEW_ACTIONBAR, showActionBar);
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

        nextButton = view.findViewById(R.id.next);
        viewPager = view.findViewById(R.id.viewpager_dataentry);
        tabLayout = view.findViewById(R.id.tablayout_data_entry);
        toolbar = view.findViewById(R.id.toolbar);
        View appBarLayout = view.findViewById(R.id.appbarlayout_data_entry);
        reportDate = view.findViewById(R.id.report_date);
        incidentDateLayout = view.findViewById(R.id.incident_date_layout);
        incidentDate = view.findViewById(R.id.incident_date_text);
        coordinatorLayout = view.findViewById(R.id.coordinatorlayout_form);
        formSectionAdapter = new FormSectionAdapter(getFragmentManager());
        viewPager.setAdapter(formSectionAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (getArguments().getBoolean(FORM_VIEW_ACTIONBAR))
            setupActionBar();
        else {
            appBarLayout.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
        }
        coordinatesView = view.findViewById(R.id.coordinates_view);
        if (!isEnrollment) {
            coordinatesView.setVisibility(View.GONE);
        } else {
            coordinatesView.setIsBgTransparent(false);
            coordinatesView.setMapListener(this);
            coordinatesView.setCurrentLocationListener(this);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d("formfragment", "onPageSelected " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    if (viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
                        if (((DataEntryFragment) formSectionAdapter.getItem(viewPager.getCurrentItem())).checkMandatory())
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                        else {
                            displayMessage("Fill mandatory fields to continue");
                            viewPager.setCurrentItem(viewPager.getCurrentItem(), true);
                        }
                    } else if (((DataEntryFragment) formSectionAdapter.getItem(viewPager.getCurrentItem())).checkMandatory())
                        getActivity().finish();
                    else {
                        displayMessage("Fill mandatory fields to continue");
                    }
                }
                Log.d("formfragment", "onPageScrollStateChanged");
            }
        });

        setupActionBar();
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
        return undoObservable.mergeWith(RxView.clicks(nextButton).map(o -> getReportStatusFromButton()));
    }

    @Override
    public void onNext(ReportStatus reportStatus) {
        //TODO: NEXT ACTION
    }

    @NonNull
    @Override
    public Observable<String> reportDateChanged() {
        onReportDateChanged = PublishSubject.create();
        return onReportDateChanged;
    }

    @NonNull
    @Override
    public Observable<LatLng> reportCoordinatesChanged() {
        onCoordinatesChanged = PublishSubject.create();
        return onCoordinatesChanged;
    }

    @NonNull
    @Override
    public Observable<String> incidentDateChanged() {
        onIncidentDateChanged = PublishSubject.create();
        return onIncidentDateChanged;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FormViewArguments arguments = Preconditions.isNull(getArguments()
                .getParcelable(FORM_VIEW_ARGUMENTS), "formViewArguments == null");

        ((App) getActivity().getApplicationContext())
                .createFormComponent(new FormModule(arguments))
                .inject(this);

        this.isEnrollment = getArguments().getBoolean(IS_ENROLLMENT);
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
            tabLayout.setupWithViewPager(viewPager);
            if (sectionViewModels.size() == 0) {
                Log.d("EMPTY", "Show empty state");
                // TODO: Show empty state
            }
        };
    }

    @NonNull
    @Override
    public Consumer<String> renderReportDate() {
        return date -> reportDate.setText(date);
    }

    @NonNull
    @Override
    public Consumer<Pair<ProgramModel, String>> renderIncidentDate() {
        return programModelAndDate -> {
            incidentDateLayout.setHint(programModelAndDate.val0().incidentDateLabel());
            incidentDateLayout.setVisibility(View.VISIBLE);
            incidentDate.setText(programModelAndDate.val1());
            if (isEnrollment && programModelAndDate.val0().captureCoordinates()) {
                coordinatesView.setVisibility(View.VISIBLE);
            } else {
                coordinatesView.setVisibility(View.GONE);
            }
        };
    }

    @NonNull
    @Override
    public Consumer<String> renderTitle() {
        return title -> {
            if (isEnrollment)
                toolbar.setTitle(String.format(getString(R.string.enroll_in), title));
            else
                toolbar.setTitle(title);
        };
    }

    @NonNull
    @Override
    public Consumer<ReportStatus> renderStatus() {
        return eventStatus -> nextButton.setActivated(/*eventStatus == ReportStatus.COMPLETED*/true);
    }

    @NonNull
    @Override
    public Consumer<Trio<String, String, String>> finishEnrollment() {
        return trio -> {
            Bundle bundle = new Bundle();
            bundle.putString("PROGRAM_UID", trio.val0());
            bundle.putString("TEI_UID", trio.val1());
            if (!trio.val2().isEmpty()) { //val0 is enrollment uid, val1 is trackedEntityType, val2 is event uid
                FormViewArguments formViewArguments = FormViewArguments.createForEvent(trio.val2());
                startActivity(FormActivity.create(this.getAbstractActivity(), formViewArguments, isEnrollment));
            } else { //val0 is program uid, val1 is trackedEntityInstance, val2 is empty
                startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
            }
            getActivity().finish();
        };
    }

    @Override
    public void renderStatusChangeSnackBar(@NonNull ReportStatus reportStatus) {
        String snackBarMessage = reportStatus == ReportStatus.COMPLETED ? getString(R.string.completed) : getString(R.string.activated);

        Snackbar.make(coordinatorLayout, snackBarMessage, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.revert), v1 -> {
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


    private ReportStatus getReportStatusFromButton() {
        return nextButton.isActivated() ? ReportStatus.ACTIVE : ReportStatus.COMPLETED;
    }

    @Override
    public void initReportDatePicker(boolean reportAllowFutureDates, boolean incidentAllowFutureDates) {
        reportDate.setOnClickListener(v -> {
            DatePickerDialogFragment dialog = DatePickerDialogFragment.create(reportAllowFutureDates);
            dialog.show(getFragmentManager());
            dialog.setFormattedOnDateSetListener(publishReportDateChange());
        });

        incidentDate.setOnClickListener(v -> {
            DatePickerDialogFragment dialog = DatePickerDialogFragment.create(incidentAllowFutureDates);
            dialog.show(getFragmentManager());
            dialog.setFormattedOnDateSetListener(publishIncidentDateChange());
        });

        reportDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                DatePickerDialogFragment dialog = DatePickerDialogFragment.create(reportAllowFutureDates);
                dialog.show(getFragmentManager());
                dialog.setFormattedOnDateSetListener(publishReportDateChange());
            }
        });

        incidentDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                DatePickerDialogFragment dialog = DatePickerDialogFragment.create(incidentAllowFutureDates);
                dialog.show(getFragmentManager());
                dialog.setFormattedOnDateSetListener(publishIncidentDateChange());
            }
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

    @NonNull
    private DatePickerDialogFragment.FormattedOnDateSetListener publishIncidentDateChange() {
        return date -> {
            if (onIncidentDateChanged != null) {
                onIncidentDateChanged.onNext(BaseIdentifiableObject.DATE_FORMAT.format(date));
            }
        };
    }

    @Override
    public void onMapPositionClick(CoordinatesView coordinatesView) {
        this.coordinatesView = coordinatesView;
        startActivityForResult(MapSelectorActivity.create(getActivity()), Constants.RQ_MAP_LOCATION_VIEW);
    }

    @Override
    public void onCurrentLocationClick(double latitude, double longitude) {
        publishCoordinatesChanged(latitude, longitude);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.RQ_MAP_LOCATION_VIEW:
                coordinatesView.updateLocation(Double.valueOf(data.getStringExtra(MapSelectorActivity.LATITUDE)), Double.valueOf(data.getStringExtra(MapSelectorActivity.LONGITUDE)));
                publishCoordinatesChanged(Double.valueOf(data.getStringExtra(MapSelectorActivity.LATITUDE)), Double.valueOf(data.getStringExtra(MapSelectorActivity.LONGITUDE)));
                this.coordinatesView = null;
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void publishCoordinatesChanged(Double lat, Double lon) {
        if (onCoordinatesChanged != null) {
            onCoordinatesChanged.onNext(new LatLng(lat, lon));
        }
    }

    public void hideSections(String uid) {
        formPresenter.checkSections();
    }
}