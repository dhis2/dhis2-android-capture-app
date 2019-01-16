package org.dhis2.data.forms;

import android.annotation.SuppressLint;
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
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.rxbinding2.view.RxView;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryFragment;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.section.viewmodels.date.DatePickerDialogFragment;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.map.MapSelectorActivity;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.custom_views.CategoryComboDialog;
import org.dhis2.utils.custom_views.CoordinatesView;
import org.dhis2.utils.custom_views.CustomDialog;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.Preconditions;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

import static android.text.TextUtils.isEmpty;


public class FormFragment extends FragmentGlobalAbstract implements FormView, CoordinatesView.OnMapPositionClick, CoordinatesView.OnCurrentLocationClick {
    private static final String FORM_VIEW_ARGUMENTS = "formViewArguments";
    private static final String FORM_VIEW_ACTIONBAR = "formViewActionbar";
    private static final String IS_ENROLLMENT = "isEnrollment";
    private static final String FORM_VIEW_TABLAYOUT = "formViewTablayout";
    private static final int RC_GO_BACK = 101;

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
    private TextInputLayout reportDateLayout;
    private TextInputEditText reportDate;
    PublishSubject<ReportStatus> undoObservable;
    private CoordinatorLayout coordinatorLayout;
    private TextInputLayout incidentDateLayout;
    private TextInputEditText incidentDate;
    private CoordinatesView coordinatesView;
    private boolean isEnrollment;
    private Trio<String, String, String> enrollmentTrio;

    private String messageOnComplete = "";
    private boolean canComplete = true;
    private LinearLayout dateLayout;
    public View datesLayout;
    private NestedScrollView nestedScrollView;
    private final int RQ_EVENT = 9876;
    private RuleActionErrorOnCompletion errorOnCompletion;
    private RuleActionWarningOnCompletion warningOnCompletion;
    private RuleActionShowError showError;


    public FormFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(@NonNull FormViewArguments formViewArguments, boolean isEnrollment, boolean showActionBar) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putParcelable(FORM_VIEW_ARGUMENTS, formViewArguments);
        args.putBoolean(IS_ENROLLMENT, isEnrollment);
        args.putBoolean(FORM_VIEW_ACTIONBAR, showActionBar);
        boolean showTabLayout = false;
        if (showActionBar)
            showTabLayout = true;
        args.putBoolean(FORM_VIEW_TABLAYOUT, showTabLayout);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance(@NonNull FormViewArguments formViewArguments, boolean isEnrollment, boolean showActionBar, boolean showTabLayout) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putParcelable(FORM_VIEW_ARGUMENTS, formViewArguments);
        args.putBoolean(IS_ENROLLMENT, isEnrollment);
        args.putBoolean(FORM_VIEW_ACTIONBAR, showActionBar);
        args.putBoolean(FORM_VIEW_TABLAYOUT, showTabLayout);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nestedScrollView = view.findViewById(R.id.content_frame);
        dateLayout = view.findViewById(R.id.date_layout);
        nextButton = view.findViewById(R.id.next);
        viewPager = view.findViewById(R.id.viewpager_dataentry);
        tabLayout = view.findViewById(R.id.tablayout_data_entry);
        toolbar = view.findViewById(R.id.toolbar);
        View appBarLayout = view.findViewById(R.id.appbarlayout_data_entry);
        datesLayout = view.findViewById(R.id.data_entry_dates);
        reportDate = view.findViewById(R.id.report_date);
        reportDateLayout = view.findViewById(R.id.report_date_layout);
        incidentDateLayout = view.findViewById(R.id.incident_date_layout);
        incidentDate = view.findViewById(R.id.incident_date_text);
        coordinatorLayout = view.findViewById(R.id.coordinatorlayout_form);
        formSectionAdapter = new FormSectionAdapter(getChildFragmentManager());
        viewPager.setAdapter(formSectionAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (getArguments().getBoolean(FORM_VIEW_ACTIONBAR))
            setupActionBar();
        else if (getArguments().getBoolean(FORM_VIEW_TABLAYOUT)) {
            /*ViewGroup.LayoutParams lp = nestedScrollView.getLayoutParams();
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            nestedScrollView.setLayoutParams(lp);*/
            toolbar.setVisibility(View.GONE);
            datesLayout.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            tabLayout.setVisibility(View.VISIBLE);
        } else {
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

    @SuppressLint("RxDefaultScheduler")
    @NonNull
    @Override
    public Observable<ReportStatus> eventStatusChanged() {
        undoObservable = PublishSubject.create();
        return undoObservable.mergeWith(RxView.clicks(nextButton).map(o -> getReportStatusFromButton())).debounce(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onNext(ReportStatus reportStatus) {
        if (viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);

            if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1) {
                ((Button) nextButton).setText(getString(R.string.save));
            } else
                ((Button) nextButton).setText(getString(R.string.next));

        } else {
            formPresenter.checkMandatoryFields();
        }
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
    }

    @Override
    public void onResume() {
        super.onResume();
        formPresenter.onAttach(this);
    }

    @Override
    public void onPause() {
        formPresenter.onDetach();
        super.onPause();
    }

    @NonNull
    @Override
    public Consumer<List<FormSectionViewModel>> renderSectionViewModels() {
        return sectionViewModels -> {
            int currentPostion = -1;
            if (formSectionAdapter.getCount() > 0 && formSectionAdapter.areDifferentSections(sectionViewModels)) {
                currentPostion = viewPager.getCurrentItem();
                for (Fragment fragment : getChildFragmentManager().getFragments()) {
                    if (fragment instanceof DataEntryFragment) {
                        continue;
                    } else if (fragment != null) {
                        getChildFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                }
                formSectionAdapter = new FormSectionAdapter(getChildFragmentManager());
                viewPager.setAdapter(formSectionAdapter);
            }
            formSectionAdapter.swapData(sectionViewModels);
            tabLayout.setupWithViewPager(viewPager);
            if (currentPostion != -1)
                viewPager.setCurrentItem(currentPostion, false);
            if (sectionViewModels.size() == 0) {
                Log.d("EMPTY", "Show empty state");
                // TODO: Show empty state
            }
            if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1) {
                ((Button) nextButton).setText(getString(R.string.save));
            }
        };
    }

    @NonNull
    @Override
    public Consumer<Pair<ProgramModel, String>> renderReportDate() {
        return programModelAndDate -> {
            reportDate.setText(programModelAndDate.val1());
            reportDateLayout.setHint(programModelAndDate.val0().enrollmentDateLabel());
        };
    }

    @NonNull
    @Override
    public Consumer<Pair<ProgramModel, String>> renderIncidentDate() {
        return programModelAndDate -> {
            incidentDateLayout.setHint(programModelAndDate.val0().incidentDateLabel());
            incidentDateLayout.setVisibility(View.VISIBLE);
            incidentDate.setText(programModelAndDate.val1());
        };
    }

    @NonNull
    @Override
    public Consumer<Boolean> renderCaptureCoordinates() {
        return captureCoordinates -> coordinatesView.setVisibility(captureCoordinates ? View.VISIBLE : View.GONE);
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
        return eventStatus -> nextButton.setActivated(true);
    }

    @NonNull
    @Override
    public Consumer<Trio<String, String, String>> finishEnrollment() {
        return trio -> {
            enrollmentTrio = trio;
            formPresenter.checkMandatoryFields();
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
        datesLayout.requestFocus();
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
            case RQ_EVENT:
                getActivity().finish();
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

    @Override
    public void messageOnComplete(String content, boolean canComplete) {
        this.messageOnComplete = content;
        this.canComplete = canComplete;
    }

    @Override
    public void hideDates() {
        this.dateLayout.setVisibility(View.GONE);
    }

    @Override
    public void isMandatoryFieldsRequired(List<FieldViewModel> viewModels) {
        boolean mandatoryRequired = false;
        for (FieldViewModel viewModel : viewModels) {
            if (viewModel.mandatory() && isEmpty(viewModel.value()))
                mandatoryRequired = true;
        }
        if (!mandatoryRequired) {
            if (enrollmentTrio != null) {
                Bundle bundle = new Bundle();
                bundle.putString("PROGRAM_UID", enrollmentTrio.val0());
                bundle.putString("TEI_UID", enrollmentTrio.val1());
                if (!enrollmentTrio.val2().isEmpty()) { //val0 is enrollment uid, val1 is trackedEntityType, val2 is event uid
                    FormViewArguments formViewArguments = FormViewArguments.createForEvent(enrollmentTrio.val2());
                    startActivityForResult(FormActivity.create(this.getAbstractActivity(), formViewArguments, isEnrollment), RQ_EVENT);
                } else { //val0 is program uid, val1 is trackedEntityInstance, val2 is empty
                    startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
                    getActivity().finish();
                }
            } else {
                checkAction();
            }
        } else {
            showMandatoryFieldsDialog();
        }
    }

    private void checkAction() {
        CustomDialog dialog = new CustomDialog(
                getContext(),
                getString(R.string.warning_error_on_complete_title),
                messageOnComplete,
                getString(R.string.button_ok),
                getString(R.string.cancel),
                1001,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        if (canComplete)
                            getActivity().finish();
                    }

                    @Override
                    public void onNegative() {
                    }
                });
        if (!isEmpty(messageOnComplete))
            dialog.show();
        else
            getActivity().finish();
    }

    @Override
    public void showMandatoryFieldsDialog() {
        new CustomDialog(
                getAbstracContext(),
                getAbstracContext().getString(R.string.missing_mandatory_fields_title),
                isEnrollment ? getAbstracContext().getString(R.string.missing_mandatory_fields_text) :
                        getAbstracContext().getString(R.string.missing_mandatory_fields_events),
                isEnrollment ? getAbstracContext().getString(R.string.missing_mandatory_fields_go_back) :
                        getAbstracContext().getString(R.string.button_ok),
                getAbstracContext().getString(R.string.cancel),
                RC_GO_BACK,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        if (isEnrollment)
                            deleteAllSavedDataAndGoBack();
                        else
                            getActivity().finish();
                    }

                    @Override
                    public void onNegative() {
                        // do nothing
                    }
                })
                .show();
    }

    private void deleteAllSavedDataAndGoBack() {
        formPresenter.deleteCascade();
    }

    @Override
    public void onAllSavedDataDeleted() {
        if (getActivity() != null)
            getActivity().finish();
    }

    @Override
    public void onBackPressed() {
        formPresenter.checkMandatoryFields();
    }

    public RuleActionErrorOnCompletion hasErrorOnComple() {
        return errorOnCompletion;
    }

    public RuleActionShowError hasError() {
        return showError;
    }

    public String getMessageOnComplete() {
        return messageOnComplete;
    }

    @Override
    public void setErrorOnCompletion(RuleActionErrorOnCompletion errorOnCompletion) {
        this.errorOnCompletion = errorOnCompletion;
    }

    @Override
    public void setWarningOnCompletion(RuleActionWarningOnCompletion warningOnCompletion) {
        this.warningOnCompletion = warningOnCompletion;
    }

    @Override
    public void setShowError(RuleActionShowError showError) {
        this.showError = showError;
    }

    @Override
    public void showCatComboDialog(CategoryComboModel categoryComboModel, List<CategoryOptionComboModel> categoryOptionComboModels) {
        new CategoryComboDialog(getAbstracContext(), categoryComboModel, categoryOptionComboModels, 123, selectedOption -> formPresenter.saveCategoryOption(selectedOption)).show();
    }
}