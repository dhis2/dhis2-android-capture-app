package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.dataapproval.DataApproval;
import org.hisp.dhis.android.core.dataapproval.DataApprovalState;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataelement.DataElementOperand;
import org.hisp.dhis.android.core.dataset.DataInputPeriod;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetElement;
import org.hisp.dhis.android.core.dataset.Section;
import org.hisp.dhis.android.core.datavalue.DataValueObjectRepository;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.Period;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;

import static android.text.TextUtils.isEmpty;

public class DataValueRepositoryImpl implements DataValueRepository {

    private final D2 d2;
    private String dataSetUid;

    public DataValueRepositoryImpl(D2 d2, String dataSetUid) {
        this.d2 = d2;
        this.dataSetUid = dataSetUid;
    }

    @Override
    public Flowable<Period> getPeriod(String periodId) {
        return Flowable.fromCallable(() -> d2.periodModule().periods().byPeriodId().eq(periodId).one().blockingGet());
    }

    @Override
    public Flowable<List<DataInputPeriod>> getDataInputPeriod() {
        return Flowable.fromCallable(() -> d2.dataSetModule().dataSets().withDataInputPeriods().byUid().eq(dataSetUid).one().blockingGet().dataInputPeriods());
    }

    public Flowable<List<CategoryCombo>> getCatCombo(String sectionName) {

        List<String> categoryCombos = new ArrayList<>();
        List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet().dataSetElements();

        if (!sectionName.equals("NO_SECTION")) {
            List<DataElement> dataElements = d2.dataSetModule().sections().withDataElements().byDataSetUid().eq(dataSetUid).byDisplayName().eq(sectionName).one().blockingGet().dataElements();
            for (DataSetElement dataSetElement : dataSetElements) {
                for (DataElement dataElement : dataElements) {

                    if (dataSetElement.dataElement().uid().equals(dataElement.uid()))

                        if (dataSetElement.categoryCombo() != null && !categoryCombos.contains(dataSetElement.categoryCombo().uid()))
                            categoryCombos.add(dataSetElement.categoryCombo().uid());

                        else if (dataElement.categoryCombo() != null && !categoryCombos.contains(dataElement.categoryComboUid()))
                            categoryCombos.add(dataElement.categoryComboUid());
                }
            }
        } else {
            for (DataSetElement dataSetElement : dataSetElements) {
                if (dataSetElement.categoryCombo() != null)
                    categoryCombos.add(dataSetElement.categoryCombo().uid());
                else {
                    DataElement dataElement = d2.dataElementModule().dataElements().uid(dataSetElement.dataElement().uid()).blockingGet();
                    categoryCombos.add(dataElement.categoryCombo().uid());
                }
            }
        }
        return d2.categoryModule().categoryCombos().byUid().in(categoryCombos).withCategories().withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC).get().toFlowable();

    }

    @Override
    public Flowable<DataSet> getDataSet() {
        return Flowable.fromCallable(() -> d2.dataSetModule().dataSets().byUid().eq(dataSetUid).one().blockingGet());

    }

    public Completable updateValue(DataSetTableModel dataValue) {

        DataValueObjectRepository dataValueObject = d2.dataValueModule().dataValues().value(
                dataValue.period(),
                dataValue.organisationUnit(),
                dataValue.dataElement(),
                dataValue.categoryOptionCombo(),
                dataValue.attributeOptionCombo()
        );

        if (dataValue.value() != null && !dataValue.value().isEmpty())
            return dataValueObject.set(dataValue.value());
        else
            return dataValueObject.delete();

    }

    @Override
    public Flowable<Map<String, List<List<Pair<CategoryOption, Category>>>>> getCatOptions(String sectionName, String catCombo) {
        Map<String, List<List<Pair<CategoryOption, Category>>>> map = new HashMap<>();
        map.put(catCombo, getMap(catCombo));
        return Flowable.just(map);
    }


    private List<List<Pair<CategoryOption, Category>>> getMap(String catCombo) {
        List<List<Pair<CategoryOption, Category>>> finalList = new ArrayList<>();
        List<Category> categories = d2.categoryModule().categoryCombos().withCategories().withCategoryOptionCombos().byUid().eq(catCombo).one().blockingGet().categories();

        for (Category category : categories) {
            List<CategoryOption> catOptions = d2.categoryModule().categories().withCategoryOptions().byUid().eq(category.uid()).one().blockingGet().categoryOptions();
            for (CategoryOption catOption : catOptions) {
                boolean add = true;
                for (List<Pair<CategoryOption, Category>> catComboList : finalList) {
                    if (finalList.contains(Pair.create(catOption, category)))
                        add = false;
                }
                if (add) {

                    if (finalList.size() != 0 && finalList.get(finalList.size() - 1).get(0).val1().uid().equals(category.uid())) {
                        finalList.get(finalList.size() - 1).add(Pair.create(catOption, category));
                    } else {
                        List<Pair<CategoryOption, Category>> list = new ArrayList<>();
                        list.add(Pair.create(catOption, category));
                        finalList.add(list);
                    }
                }
            }
        }
        return finalList;
    }


    private DataElement transformDataElement(DataElement dataElement, List<DataSetElement> override) {
        for (DataSetElement dataSetElement : override)
            if (dataSetElement.dataElement().uid().equals(dataElement.uid()) && dataSetElement.categoryCombo() != null)
                return DataElement.builder()
                        .uid(dataElement.uid())
                        .code(dataElement.code())
                        .name(dataElement.name())
                        .displayName(dataElement.displayName())
                        .shortName(dataElement.shortName())
                        .displayShortName(dataElement.displayShortName())
                        .description(dataElement.description())
                        .displayDescription(dataElement.displayDescription())
                        .valueType(dataElement.valueType())
                        .zeroIsSignificant(dataElement.zeroIsSignificant())
                        .aggregationType(dataElement.aggregationType())
                        .formName(dataElement.formName())
                        .domainType(dataElement.domainType())
                        .displayFormName(dataElement.displayFormName())
                        .optionSet(dataElement.optionSet())
                        .categoryCombo(dataSetElement.categoryCombo()).build();

        return dataElement;
    }

    @Override
    public Flowable<List<DataSetTableModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb, String sectionName) {

        Map<String, String> mapDataElementCatCombo = new HashMap<>();

        return Flowable.just(d2.dataSetModule().dataSets().withDataSetElements().byUid().eq(dataSetUid).one().blockingGet())
                .flatMapIterable(dataSet -> {
                    List<DataSetElement> dataElements = new ArrayList<>();
                    if (!sectionName.equals("NO_SECTION")) {
                        List<DataElement> dataElementSection = d2.dataSetModule().sections().withDataElements()
                                .byDataSetUid().eq(dataSetUid).byDisplayName().eq(sectionName).one().blockingGet().dataElements();
                        for (DataElement dataElement : dataElementSection) {
                            for (DataSetElement dataSetElement : dataSet.dataSetElements())
                                if (dataSetElement.dataElement().uid().equals(dataElement.uid()))
                                    dataElements.add(dataSetElement);
                        }
                    } else
                        dataElements = dataSet.dataSetElements();

                    return dataElements;
                })
                .flatMapIterable(dataSetElement -> {
                            if (dataSetElement.categoryCombo() != null)
                                mapDataElementCatCombo.put(dataSetElement.dataElement().uid(), dataSetElement.categoryCombo().uid());
                            else
                                mapDataElementCatCombo.put(dataSetElement.dataElement().uid(),
                                        d2.dataElementModule().dataElements().byUid().eq(dataSetElement.dataElement().uid()).one().blockingGet().categoryCombo().uid());

                            return d2.dataValueModule().dataValues().byDataElementUid().eq(dataSetElement.dataElement().uid())
                                    .byAttributeOptionComboUid().eq(catOptionComb)
                                    .byPeriod().eq(initPeriodType)
                                    .byOrganisationUnitUid().eq(orgUnitUid)
                                    .byDeleted().isFalse()
                                    .blockingGet();
                        }
                ).map(dataValue -> {
                    List<CategoryOption> categoryOptions = d2.categoryModule().categoryOptionCombos().withCategoryOptions()
                            .byUid().eq(dataValue.categoryOptionCombo()).one().blockingGet().categoryOptions();
                    List<String> uidCatOptions = new ArrayList<>();
                    for (CategoryOption catOption : categoryOptions)
                        uidCatOptions.add(catOption.uid());

                    DataElement dataElement = d2.dataElementModule()
                            .dataElements()
                            .uid(dataValue.dataElement())
                            .blockingGet();

                    String value = dataValue.value();

                    if (dataElement.optionSetUid() != null &&
                            !dataElement.optionSetUid().isEmpty() && !isEmpty(value)) {
                        Option option = d2.optionModule().options()
                                .byOptionSetUid().eq(dataElement.optionSetUid())
                                .byCode().eq(value).one().blockingGet();
                        if (option != null) {
                            value = option.displayName();
                        }
                    }
                    return DataSetTableModel.create(dataValue.id(), dataValue.dataElement(), dataValue.period(),
                            dataValue.organisationUnit(), dataValue.categoryOptionCombo(), dataValue.attributeOptionCombo(),
                            value, dataValue.storedBy(), "",//no used anywhere, remove this field
                            uidCatOptions, mapDataElementCatCombo.get(dataValue.dataElement()));

                }).toList().toFlowable();
    }

    @Override
    public Flowable<List<DataElementOperand>> getCompulsoryDataElements() {
        return d2.dataSetModule().dataSets().withCompulsoryDataElementOperands().uid(dataSetUid).get()
                .map(DataSet::compulsoryDataElementOperands).toFlowable();
    }

    @Override
    public Flowable<List<DataElementOperand>> getGreyFields(String sectionName) {
        if (!sectionName.isEmpty() && !sectionName.equals("NO_SECTION"))
            return d2.dataSetModule().sections().withGreyedFields().byDataSetUid().eq(dataSetUid).byDisplayName().eq(sectionName).one().get()
                    .map(Section::greyedFields).toFlowable();
        else
            return Flowable.just(new ArrayList<>());
    }

    @Override
    public Flowable<Section> getSectionByDataSet(String section) {
        if (!section.isEmpty() && !section.equals("NO_SECTION"))
            return Flowable.just(d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid).byDisplayName().eq(section).one().blockingGet());
        else
            return Flowable.just(Section.builder().uid("").build());

    }

    @Override
    public Flowable<Boolean> completeDataSet(String orgUnitUid, String periodInitialDate, String catCombo) {

        d2.dataSetModule().dataSetCompleteRegistrations().value(
                periodInitialDate,
                orgUnitUid,
                dataSetUid,
                catCombo
        ).blockingSet();

        return d2.dataSetModule().dataSetCompleteRegistrations().value(
                periodInitialDate,
                orgUnitUid,
                dataSetUid,
                catCombo
        )
                .exists()
                .toFlowable();
    }

    @Override
    public Flowable<Boolean> reopenDataSet(String orgUnitUid, String periodInitialDate, String catCombo) {

        d2.dataSetModule().dataSetCompleteRegistrations().value(
                periodInitialDate,
                orgUnitUid,
                dataSetUid,
                catCombo
        ).blockingDeleteIfExist();
        return d2.dataSetModule().dataSetCompleteRegistrations().value(
                periodInitialDate,
                orgUnitUid,
                dataSetUid,
                catCombo
        ).exists()
                .map(exist -> !exist)
                .toFlowable();
    }

    @Override
    public Flowable<Boolean> isCompleted(String orgUnitUid, String periodInitialDate, String catCombo) {

        return Flowable.fromCallable(() -> {
            DataSetCompleteRegistration completeRegistration = d2.dataSetModule().dataSetCompleteRegistrations()
                    .byDataSetUid().eq(dataSetUid)
                    .byAttributeOptionComboUid().eq(catCombo)
                    .byPeriod().eq(periodInitialDate)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .one().blockingGet();
            return completeRegistration != null && !completeRegistration.deleted();
        });
    }

    @Override
    public Flowable<Boolean> isApproval(String orgUnit, String period, String attributeOptionCombo) {
        return Flowable.fromCallable(() -> {
            DataApproval dataApproval = d2.dataSetModule().dataApprovals()
                    .byOrganisationUnitUid().eq(orgUnit)
                    .byPeriodId().eq(period)
                    .byAttributeOptionComboUid().eq(attributeOptionCombo)
                    .one().blockingGet();
            return dataApproval != null && dataApproval.state().equals(DataApprovalState.APPROVED_HERE);
        });
    }

    @Override
    public Flowable<List<DataElement>> getDataElements(CategoryCombo categoryCombo, String sectionName) {
        if (!sectionName.equals("NO_SECTION")) {
            List<DataElement> listDataElements = d2.dataSetModule().sections().withDataElements().byDataSetUid().eq(dataSetUid).byDisplayName().eq(sectionName).one().blockingGet().dataElements();
            List<DataElement> dataElementsOverride = new ArrayList<>();
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet().dataSetElements();

            for (DataElement de : listDataElements) {
                DataElement override = transformDataElement(de, dataSetElements);
                if (override.categoryComboUid().equals(categoryCombo.uid()))
                    dataElementsOverride.add(override);
            }

            return Flowable.just(dataElementsOverride);
        } else {
            List<String> dataElementUids = new ArrayList<>();
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets().withDataSetElements().byUid().eq(dataSetUid).one().blockingGet().dataSetElements();
            for (DataSetElement dataSetElement : dataSetElements) {
                if (dataSetElement.categoryCombo() != null && categoryCombo.uid().equals(dataSetElement.categoryCombo().uid()))
                    dataElementUids.add(dataSetElement.dataElement().uid());
                else {
                    String uid = d2.dataElementModule().dataElements().uid(dataSetElement.dataElement().uid()).blockingGet().categoryComboUid();
                    if (categoryCombo.uid().equals(uid))
                        dataElementUids.add(dataSetElement.dataElement().uid());
                }
            }
            return d2.dataElementModule().dataElements()
                    .byUid().in(dataElementUids)
                    .orderByName(RepositoryScope.OrderByDirection.ASC)
                    .get().toFlowable();
        }
    }

    @Override
    public List<CategoryOption> getCatOptionFromCatOptionCombo(CategoryOptionCombo categoryOptionCombo) {
        return d2.categoryModule().categoryOptionCombos().withCategoryOptions().uid(categoryOptionCombo.uid()).blockingGet().categoryOptions();
    }


    @Override
    public CategoryOption getCatOptionFromUid(String catOption) {
        return d2.categoryModule().categoryOptions().uid(catOption).blockingGet();
    }

    @Override
    public Flowable<Boolean> canWriteAny() {
        return d2.dataSetModule().dataSets().uid(dataSetUid).get().toFlowable()
                .flatMap(dataSet -> {
                    if (dataSet.access().data().write())
                        return d2.categoryModule().categoryOptionCombos().withCategoryOptions()
                                .byCategoryComboUid().eq(dataSet.categoryCombo().uid()).get().toFlowable()
                                .map(categoryOptionCombos -> {
                                    boolean canWriteCatOption = false;
                                    for (CategoryOptionCombo categoryOptionCombo : categoryOptionCombos) {
                                        for (CategoryOption categoryOption : categoryOptionCombo.categoryOptions())
                                            if (categoryOption.access().data().write()) {
                                                canWriteCatOption = true;
                                                break;
                                            }
                                    }
                                    boolean canWriteOrgUnit = false;

                                    if (canWriteCatOption) {
                                        List<OrganisationUnit> organisationUnits = d2.organisationUnitModule().organisationUnits()
                                                .byDataSetUids(Collections.singletonList(dataSetUid))
                                                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).blockingGet();

                                        canWriteOrgUnit = !organisationUnits.isEmpty();

                                    }

                                    return canWriteCatOption && canWriteOrgUnit;

                                });
                    else
                        return Flowable.just(false);
                });

    }

    @Override
    public @NotNull List<CategoryOptionCombo> getCatOptionComboFrom(@Nullable String catComboUid,
                                                                    List<List<CategoryOption>> catOptionsList) {
        List<CategoryOptionCombo> catOptionCombos = new ArrayList<>();

        for (List<CategoryOption> catOptions : catOptionsList) {
            catOptionCombos.addAll(d2.categoryModule().categoryOptionCombos()
                    .byCategoryOptions(UidsHelper.getUidsList(catOptions))
                    .byCategoryComboUid().eq(catComboUid)
                    .blockingGet());
        }

        return catOptionCombos;
    }
}
