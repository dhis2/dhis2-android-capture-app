package org.dhis2.utils.filters.workingLists

import org.dhis2.utils.filters.FilterManager
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkingListItemTest() {

    private val workingListItem = WorkingListItem("uid", "displayName")

    @After
    fun tearDown() {
        FilterManager.getInstance().currentWorkingList(null)
    }

    @Test
    fun `Should set to current working list`() {
        workingListItem.select()
        assertTrue(FilterManager.getInstance().currentWorkingList() == workingListItem)
    }

    @Test
    fun `Should clear working list if active`() {
        workingListItem.select()
        workingListItem.select()
        assertTrue(FilterManager.getInstance().currentWorkingList() == null)
    }

    @Test
    fun `Should return true if selected`() {
        workingListItem.select()
        assertTrue(workingListItem.isSelected())
    }

    @Test
    fun `Should return false if not selected`() {
        assertTrue(!workingListItem.isSelected())
    }
}
