package org.dhis2.utils.customviews

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class CategoryComboDIalogPresenterTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private lateinit var presenter: CategoryComboDialogPresenter

    @Before
    fun setUp() {
        presenter = CategoryComboDialogPresenter(d2, "categoryComboUid")
    }

    @Test
    fun `Should return category option combo uid for a given list of options`() {

        whenever(d2.categoryModule().categoryOptionCombos().byCategoryOptions(UidsHelper.getUidsList(getTestingOptions())).one().blockingGet()) doReturn getTestingCatOptCombo()

        val actualCatOptCombo = presenter.getCatOptionCombo(getTestingOptions())

        Assert.assertEquals(actualCatOptCombo, "categoryOptionComboUid")
    }

    private fun getTestingCatOptCombo(): CategoryOptionCombo {
        return CategoryOptionCombo.builder()
                .uid("categoryOptionComboUid")
                .categoryOptions(
                        getTestingOptions()
                )
                .build()
    }

    private fun getTestingOptions(): List<CategoryOption> {
        return arrayListOf(
                CategoryOption.builder().uid("option1").name("option1").code("option1").build(),
                CategoryOption.builder().uid("option2").name("option2").code("option2").build()
        )
    }
}