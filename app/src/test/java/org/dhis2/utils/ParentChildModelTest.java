package org.dhis2.utils;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ParentChildModelTest {

    @Test
    public void testCreateParentChildModel() {
        OrganisationUnitModel orgToAdd1 = OrganisationUnitModel.builder()
                .uid("XXXX1")
                .level(1)
                .parent("XXXX2")
                .name("Path name")
                .displayName("Display name")
                .displayShortName("Display short name")
                .build();

        OrganisationUnitModel orgToAdd2 = OrganisationUnitModel.builder()
                .uid("XXXX3")
                .level(1)
                .parent("XXXX4")
                .name("Path name")
                .displayName("Display name")
                .displayShortName("Display short name")
                .build();

        ParentChildModel<OrganisationUnitModel> orgUnitParent1 =
                ParentChildModel.create(orgToAdd1, new ArrayList<>(), true);

        ParentChildModel<OrganisationUnitModel> orgUnitParent2 =
                ParentChildModel.create(orgToAdd2, new ArrayList<>(), true);

        List<ParentChildModel<OrganisationUnitModel>> parentChildModels = new ArrayList<>();
        parentChildModels.add(orgUnitParent1);
        parentChildModels.add(orgUnitParent2);

        ParentChildModel<OrganisationUnitModel> orgUnitParent3 =
                ParentChildModel.create(orgToAdd2, parentChildModels, true);


        assertNotNull(orgUnitParent1);
        assertTrue(orgUnitParent1.isSelectable());

        orgUnitParent1.addItem(orgUnitParent2);

        assertEquals(1, orgUnitParent1.childs().size());

        assertEquals(2, orgUnitParent3.childs().size());
    }
}
