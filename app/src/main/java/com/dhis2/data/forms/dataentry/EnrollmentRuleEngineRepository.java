package com.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.FormRepository;
import com.dhis2.utils.Result;
import com.squareup.sqlbrite.BriteDatabase;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.rules.models.RuleAttributeValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEnrollment;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

import static hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Flowable;

final class EnrollmentRuleEngineRepository implements RuleEngineRepository {
    private static final String QUERY_ENROLLMENT = "SELECT\n" +
            "  uid,\n" +
            "  incidentDate,\n" +
            "  enrollmentDate,\n" +
            "  status\n" +
            "FROM Enrollment\n" +
            "WHERE uid = ? \n" +
            "LIMIT 1;";

    private static final String QUERY_ATTRIBUTE_VALUES = "SELECT\n" +
            "  Field.id,\n" +
            "  Value.value\n" +
            "FROM (Enrollment INNER JOIN Program ON Program.uid = Enrollment.program)\n" +
            "  INNER JOIN (\n" +
            "      SELECT\n" +
            "        TrackedEntityAttribute.uid AS id,\n" +
            "        ProgramTrackedEntityAttribute.program AS program\n" +
            "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n" +
            "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "    ) AS Field ON Field.program = Program.uid\n" +
            "  INNER JOIN TrackedEntityAttributeValue AS Value ON (\n" +
            "    Value.trackedEntityAttribute = Field.id\n" +
            "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n" +
            "WHERE Enrollment.uid = ? AND Value.value IS NOT NULL;";

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final FormRepository formRepository;

    @NonNull
    private final String enrollmentUid;

    EnrollmentRuleEngineRepository(
            @NonNull BriteDatabase briteDatabase,
            @NonNull FormRepository formRepository,
            @NonNull String enrollmentUid) {
        this.briteDatabase = briteDatabase;
        this.formRepository = formRepository;
        this.enrollmentUid = enrollmentUid;
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryAttributeValues()
                .switchMap(this::queryEnrollment)
                .switchMap(enrollment -> formRepository.ruleEngine()
                        .switchMap(ruleEngine -> Flowable.fromCallable(ruleEngine.evaluate(enrollment))
                                .map(Result::success)
                                .onErrorReturn(error -> Result.failure(new Exception(error)))
                        )
                );
    }

    @NonNull
    private Flowable<RuleEnrollment> queryEnrollment(
            @NonNull List<RuleAttributeValue> attributeValues) {
        return toV2Flowable(briteDatabase.createQuery(EnrollmentModel.TABLE, QUERY_ENROLLMENT, enrollmentUid)
                .mapToOne(cursor -> {
                    Date enrollmentDate = parseDate(cursor.getString(2));
                    Date incidentDate = cursor.isNull(1) ?
                            enrollmentDate : parseDate(cursor.getString(1));
                    RuleEnrollment.Status status = RuleEnrollment.Status
                            .valueOf(cursor.getString(3));

                    return RuleEnrollment.create(cursor.getString(0),
                            incidentDate, enrollmentDate, status, attributeValues);
                }));
    }

    @NonNull
    private Flowable<List<RuleAttributeValue>> queryAttributeValues() {
        return toV2Flowable(briteDatabase.createQuery(Arrays.asList(EnrollmentModel.TABLE,
                TrackedEntityAttributeValueModel.TABLE), QUERY_ATTRIBUTE_VALUES, enrollmentUid)
                .mapToList(cursor -> RuleAttributeValue.create(
                        cursor.getString(0), cursor.getString(1))
                ));
    }

    @NonNull
    private static Date parseDate(@NonNull String date) {
        try {
            return BaseIdentifiableObject.DATE_FORMAT.parse(date);
        } catch (ParseException parseException) {
            throw new RuntimeException(parseException);
        }
    }
}
