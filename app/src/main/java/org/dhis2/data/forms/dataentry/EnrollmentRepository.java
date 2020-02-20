package org.dhis2.data.forms.dataentry;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DhisTextUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.fileresource.FileResource;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramSection;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;


public final class EnrollmentRepository implements DataEntryRepository {

    @NonNull
    private final FieldViewModelFactory fieldFactory;

    @NonNull
    private final String enrollmentUid;
    private final Context context;
    private final D2 d2;
    private final EnrollmentObjectRepository enrollmentRepository;
    
    public static final String ENROLLMENT_DATA_SECTION_UID = "ENROLLMENT_DATA_SECTION_UID";
    public static final String ENROLLMENT_DATE_UID = "enrollmentDateUid";
    public static final String INCIDENT_DATE_UID = "incidentDateUid";
    public static final String ORG_UNIT_UID = "orgUnitUid";
    public static final String TEI_COORDINATES_UID = "teiCoordinatesUid";
    public static final String ENROLLMENT_COORDINATES_UID = "enrollmentCoordinatesUid";

    public EnrollmentRepository(@NonNull Context context,
                                @NonNull FieldViewModelFactory fieldFactory,
                                @NonNull String enrollmentUid, D2 d2) {
        this.fieldFactory = fieldFactory;
        this.enrollmentUid = enrollmentUid;
        this.enrollmentRepository = d2.enrollmentModule().enrollments().uid(enrollmentUid);
        this.context = context;
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list() {

        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .flatMap(enrollment -> d2.programModule().programs().uid(enrollment.program()).get())
                .flatMap(program -> d2.programModule().programSections().byProgramUid().eq(program.uid()).withAttributes().get()
                        .flatMap(programSections -> {
                            if (programSections.isEmpty()) {
                                return getFieldsForSingleSection(program.uid())
                                        .map(singleSectionList -> {
                                            List<FieldViewModel> list = getSingleSection();
                                            list.addAll(singleSectionList);
                                            return list;
                                        });
                            } else {
                                return getFieldsForMultipleSections(programSections, program.uid());
                            }
                        }).map(list -> {
                            List<FieldViewModel> fields = getEnrollmentData(program);
                            fields.addAll(list);
                            return fields;
                        })
                ).toFlowable();
    }

    @VisibleForTesting()
    public Single<List<FieldViewModel>> getFieldsForSingleSection(String programUid) {
        return d2.programModule().programTrackedEntityAttributes().withRenderType()
                .byProgram().eq(programUid).get()
                .toFlowable()
                .flatMapIterable(programTrackedEntityAttributes -> programTrackedEntityAttributes)
                .map(this::transform)
                .toList();
    }

    @VisibleForTesting()
    public Single<List<FieldViewModel>> getFieldsForMultipleSections(List<ProgramSection> programSections, String programUid) {
        List<FieldViewModel> fields = new ArrayList<>();
        for (ProgramSection section : programSections) {
            fields.add(transformSection(section));
            for (TrackedEntityAttribute attribute : section.attributes()) {
                ProgramTrackedEntityAttribute programTrackedEntityAttribute =
                        d2.programModule().programTrackedEntityAttributes()
                                .withRenderType()
                                .byProgram().eq(programUid)
                                .byTrackedEntityAttribute().eq(attribute.uid())
                                .one().blockingGet();
                fields.add(transform(programTrackedEntityAttribute));
            }
        }
        return Single.just(fields);
    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits() {
        return d2.organisationUnitModule().organisationUnits().byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get().toObservable();
    }


    @NonNull
    private FieldViewModel transform(@NonNull ProgramTrackedEntityAttribute programTrackedEntityAttribute) {
        TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes().uid(programTrackedEntityAttribute.trackedEntityAttribute().uid())
                .blockingGet();
        TrackedEntityAttributeValueObjectRepository attrValueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
                .value(attribute.uid(), enrollmentRepository.blockingGet().trackedEntityInstance());

        String uid = attribute.uid();
        String label = attribute.displayName();
        ValueType valueType = attribute.valueType();
        boolean mandatory = programTrackedEntityAttribute.mandatory();
        String optionSet = attribute.optionSet() != null ? attribute.optionSet().uid() : null;
        boolean allowFutureDates = programTrackedEntityAttribute.allowFutureDate() != null ? programTrackedEntityAttribute.allowFutureDate() : false;
        boolean generated = attribute.generated();

        String orgUnitUid = enrollmentRepository.blockingGet().organisationUnit();

        String dataValue = attrValueRepository.blockingExists() ? attrValueRepository.blockingGet().value() : null;

        String description = attribute.displayDescription();
        String formName = attribute.formName(); //TODO: WE NEED THE DISPLAY FORM NAME WHEN AVAILABLE IN THE SDK
        String fieldMask = attribute.fieldMask();


        if (valueType == ValueType.IMAGE && !DhisTextUtils.Companion.isEmpty(dataValue)) {
            FileResource fileResource = d2.fileResourceModule().fileResources().uid(dataValue).blockingGet();
            if (fileResource != null)
                dataValue = fileResource.path();
        }

        int optionCount = 0;
        if (!DhisTextUtils.Companion.isEmpty(optionSet)) {
            optionCount = d2.optionModule().options().byOptionSetUid().eq(optionSet).blockingCount();

            if (!DhisTextUtils.Companion.isEmpty(dataValue)) {
                if (d2.optionModule().options()
                        .byOptionSetUid().eq(optionSet).byCode().eq(dataValue)
                        .one().blockingExists()) {
                    dataValue = d2.optionModule().options()
                            .byOptionSetUid().eq(optionSet)
                            .byCode().eq(dataValue).one().blockingGet().displayName();
                }
            }
        }
        String warning = null;

        if (generated && dataValue == null) {
            try {
                String teiUid = enrollmentRepository.blockingGet().trackedEntityInstance();

                //checks if tei has been deleted
                if (teiUid != null) {
                    try {
                        dataValue = d2.trackedEntityModule().reservedValueManager().blockingGetValue(uid, orgUnitUid);
                    } catch (Exception e) {
                        dataValue = null;
                        warning = context.getString(R.string.no_reserved_values);
                    }

                    //Checks if ValueType is Numeric and that it start with a 0, then removes the 0
                    if (valueType == ValueType.NUMBER)
                        while (dataValue.startsWith("0")) {
                            dataValue = d2.trackedEntityModule().reservedValueManager().blockingGetValue(uid, orgUnitUid);
                        }

                    if (!DhisTextUtils.Companion.isEmpty(dataValue)) {
                        attrValueRepository.blockingSet(dataValue);
                    } else
                        mandatory = true;
                }
            } catch (Exception e) {
                Timber.e(e);
                warning = context.getString(R.string.no_reserved_values);
                mandatory = true;
            }
        }
        ValueTypeDeviceRendering fieldRendering = programTrackedEntityAttribute.renderType() != null ?
                programTrackedEntityAttribute.renderType().mobile() : null;

        ObjectStyle objectStyle = attribute.style() != null ? attribute.style() : ObjectStyle.builder().build();

        if (valueType == ValueType.ORGANISATION_UNIT && !DhisTextUtils.Companion.isEmpty(dataValue)) {
            dataValue = getOrgUnitValue(dataValue);
        }

        if (warning != null) {
            return fieldFactory.create(uid,
                    label, valueType, mandatory, optionSet, dataValue, null, allowFutureDates,
                    false, null, description, fieldRendering, optionCount, objectStyle, fieldMask)
                    .withWarning(warning);
        } else {
            return fieldFactory.create(uid,
                    label, valueType, mandatory, optionSet, dataValue, null, allowFutureDates,
                    !generated, null, description, fieldRendering, optionCount, objectStyle, fieldMask);
        }
    }

    private List<FieldViewModel> getSingleSection() {
        List<FieldViewModel> singleSectionList = new ArrayList<>();
        singleSectionList.add(
                SectionViewModel.create(
                        "singleSection",
                        "Enrollment form",
                        null,
                        false,
                        0,
                        0,
                        ProgramStageSectionRenderingType.LISTING.name())
        );

        return singleSectionList;
    }

    private List<FieldViewModel> getEnrollmentData(Program program) {
        List<FieldViewModel> enrollmendDataList = new ArrayList<>();
        enrollmendDataList.add(getEnrollmentDataSection(program.description()));
        enrollmendDataList.add(getEnrollmentDateField(program.enrollmentDateLabel(), program.selectEnrollmentDatesInFuture(), false)); //TODO: CHECK IF CAN BE EDITED
        if (program.displayIncidentDate()) {
            enrollmendDataList.add(getIncidentDateField(program.incidentDateLabel(), program.selectIncidentDatesInFuture(), false)); //TODO: CHECK IF CAN BE EDITED
        }
        enrollmendDataList.add(getOrgUnitField("enrolling Org. Unit", false)); //TODO: CHECK IF CAN BE EDITED

        TrackedEntityType teiType = d2.trackedEntityModule().trackedEntityTypes().uid(program.trackedEntityType().uid()).blockingGet();
        if (teiType.featureType() != null && teiType.featureType() != FeatureType.NONE) {
            enrollmendDataList.add(getTeiCoordinatesField("tei coordinates", teiType.featureType()));
        }

        if (program.featureType() != null && program.featureType() != FeatureType.NONE) {
            enrollmendDataList.add(getEnrollmentCoordinatesField("enrollment coordinates", program.featureType()));
        }

        return enrollmendDataList;
    }

    private FieldViewModel getEnrollmentDataSection(String description) {
        return SectionViewModel.create(
                ENROLLMENT_DATA_SECTION_UID,
                "Enrollment data",
                description,
                false,
                0,
                0,
                ProgramStageSectionRenderingType.LISTING.name()
        );
    }

    private FieldViewModel getEnrollmentDateField(String enrollmentDateLabel, Boolean allowFutureDates, Boolean editable) {
        return DateTimeViewModel.create(
                ENROLLMENT_DATE_UID,
                enrollmentDateLabel,
                true,
                ValueType.DATE,
                DateUtils.databaseDateFormat().format(enrollmentRepository.blockingGet().enrollmentDate()),
                ENROLLMENT_DATA_SECTION_UID,
                allowFutureDates,
                editable,
                null,
                ObjectStyle.builder().build()
        );
    }

    private FieldViewModel getIncidentDateField(String incidentDateLabel, Boolean allowFutureDates, boolean editable) {
        return DateTimeViewModel.create(
                INCIDENT_DATE_UID,
                incidentDateLabel,
                true,
                ValueType.DATE,
                DateUtils.databaseDateFormat().format(enrollmentRepository.blockingGet().incidentDate()),
                ENROLLMENT_DATA_SECTION_UID,
                allowFutureDates,
                editable,
                null,
                ObjectStyle.builder().build()
        );
    }

    private FieldViewModel getOrgUnitField(String enrollmentOrgUnitLabel, boolean editable) {
        return OrgUnitViewModel.create(
                ORG_UNIT_UID,
                enrollmentOrgUnitLabel,
                true,
                getOrgUnitValue(enrollmentRepository.blockingGet().organisationUnit()),
                ENROLLMENT_DATA_SECTION_UID,
                editable,
                null,
                ObjectStyle.builder().build()
        );
    }

    private FieldViewModel getTeiCoordinatesField(String teiCoordinatesLabel, FeatureType featureType) {
        TrackedEntityInstance tei = d2.trackedEntityModule().trackedEntityInstances()
                .uid(
                        enrollmentRepository.blockingGet().trackedEntityInstance()
                ).blockingGet();
        return CoordinateViewModel.create(
                TEI_COORDINATES_UID,
                teiCoordinatesLabel,
                false,
                tei.geometry() != null ? tei.geometry().coordinates() : null,
                ENROLLMENT_DATA_SECTION_UID,
                true,
                null,
                ObjectStyle.builder().build(),
                featureType
        );
    }

    private FieldViewModel getEnrollmentCoordinatesField(String enrollmentCoordinatesLabel, FeatureType featureType) {
        return CoordinateViewModel.create(
                ENROLLMENT_COORDINATES_UID,
                enrollmentCoordinatesLabel,
                false,
                enrollmentRepository.blockingGet().geometry() != null ?
                        enrollmentRepository.blockingGet().geometry().coordinates() : null,
                ENROLLMENT_DATA_SECTION_UID,
                true,
                null,
                ObjectStyle.builder().build(),
                featureType
        );
    }

    private FieldViewModel transformSection(ProgramSection programSection) {
        return SectionViewModel.create(
                programSection.uid(),
                programSection.displayName(),
                programSection.description(),
                false,
                0,
                0,
                ProgramStageSectionRenderingType.LISTING.name());
    }

    private String getOrgUnitValue(String currentValueUid) {
        if (currentValueUid != null) {
            return currentValueUid + "_ou_" + d2.organisationUnitModule().organisationUnits().uid(currentValueUid).blockingGet().displayName();
        } else {
            return null;
        }
    }
}