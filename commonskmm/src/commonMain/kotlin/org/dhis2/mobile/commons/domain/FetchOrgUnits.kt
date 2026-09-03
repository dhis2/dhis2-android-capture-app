package org.dhis2.mobile.commons.domain

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.commons.data.OrgUnitTreeRepository
import org.dhis2.mobile.commons.model.internal.DomainOrgUnit
import org.hisp.dhis.mobile.ui.designsystem.component.OrgTreeItem

class FetchOrgUnits(
    private val orgUnitTreeRepository: OrgUnitTreeRepository,
) : UseCase<FetchOrgUnitsInput, List<OrgTreeItem>> {
    override suspend fun invoke(input: FetchOrgUnitsInput) =
        try {
            val orgUnits = orgUnitTreeRepository.orgUnits(input.query)
            val uidsInList = orgUnits.map { it.uid }.toSet()
            val labelOccurrences = orgUnits.groupingBy { it.label }.eachCount()

            val list =
                orgUnits.map { orgUnit ->
                    val hasDuplicatedLabel = (labelOccurrences[orgUnit.label] ?: 0) > 1
                    val parentIsMissing = orgUnit.parentUid()?.let { it !in uidsInList } ?: false
                    OrgTreeItem(
                        uid = orgUnit.uid,
                        label = orgUnit.label,
                        tag =
                            orgUnit
                                .closestDifferentParentName(orgUnits)
                                ?.takeIf { hasDuplicatedLabel && parentIsMissing },
                        isOpen = true,
                        hasChildren = orgUnitTreeRepository.orgUnitHasChildren(orgUnit.uid),
                        selected = input.selectedOrgUnits.contains(orgUnit.uid),
                        level = orgUnit.level,
                        selectedChildrenCount =
                            orgUnitTreeRepository.countSelectedChildren(
                                orgUnit.uid,
                                input.selectedOrgUnits,
                            ),
                        canBeSelected = orgUnitTreeRepository.canBeSelected(orgUnit.uid),
                    )
                }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}

data class FetchOrgUnitsInput(
    val query: String?,
    val selectedOrgUnits: List<String>,
)

private fun DomainOrgUnit.parentUid(): String? =
    path
        .trim('/')
        .substringBeforeLast('/', missingDelimiterValue = "")
        .split("/")
        .ifEmpty { null }
        ?.lastOrNull()

private fun DomainOrgUnit.closestDifferentParentName(orgUnits: List<DomainOrgUnit>): String? =
    namePath.find { name -> orgUnits.any { !it.namePath.contains(name) } }
