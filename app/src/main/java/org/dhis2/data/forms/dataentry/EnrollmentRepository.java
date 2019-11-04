package org.dhis2.data.forms.dataentry;

import android.content.Context;

import androidx.annotation.NonNull;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.utils.FileResourcesUtil;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.fileresource.FileResource;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

public final class EnrollmentRepository implements DataEntryRepository {

    @NonNull
    private final FieldViewModelFactory fieldFactory;

    @NonNull
    private final String enrollmentUid;
    private final Context context;
    private final D2 d2;
    private final EnrollmentObjectRepository enrollmentRepository;

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
                .flatMap(enrollment -> d2.programModule().programTrackedEntityAttributes().byProgram().eq(enrollment.program()).get()).toFlowable()
                .flatMapIterable(programTrackedEntityAttributes -> programTrackedEntityAttributes)
                .map(this::transform).toList().toFlowable();
    }

    @Override
    public Observable<List<OrganisationUnitLevel>> getOrgUnitLevels() {
        return Observable.just(d2.organisationUnitModule().organisationUnitLevels().blockingGet());
    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits() {
        return d2.organisationUnitModule().organisationUnits().byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get().toObservable();
    }


    @NonNull
    private FieldViewModel transform(@NonNull ProgramTrackedEntityAttribute programTrackedEntityAttribute) {
        TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes().withObjectStyle().uid(programTrackedEntityAttribute.trackedEntityAttribute().uid())
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


        if (valueType == ValueType.IMAGE && !isEmpty(dataValue)) {
            FileResource fileResource = d2.fileResourceModule().fileResources().uid(dataValue).blockingGet();
            if (fileResource != null)
                dataValue = fileResource.path();
        }

        int optionCount = 0;
        if (!isEmpty(optionSet)) {
            optionCount = d2.optionModule().options().byOptionSetUid().eq(optionSet).blockingCount();

            if (!isEmpty(dataValue)) {
                dataValue = d2.optionModule().options()
                        .byOptionSetUid().eq(optionSet)
                        .byCode().eq(dataValue).one().blockingGet().displayName();
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

                    if (!isEmpty(dataValue)) {
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

        if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits().uid(dataValue).blockingGet().displayName();
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
}