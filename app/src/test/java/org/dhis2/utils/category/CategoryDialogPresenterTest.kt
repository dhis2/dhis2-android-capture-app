package org.dhis2.utils.category

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.junit.Test
import org.mockito.Mockito

class CategoryDialogPresenterTest {
    lateinit var presenter: CategoryDialogPresenter
    private val catOptMapper: CategoryOptionCategoryDialogItemMapper = mock()
    private val catOptCombMapper: CategoryOptionComboCategoryDialogItemMapper = mock()
    val view: CategoryDialogView = mock()
    private val schedulerProvider: SchedulerProvider = TrampolineSchedulerProvider()
    val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Test
    fun `Should init for category options`() {
        val presenter = CategoryDialogPresenter(
            view,
            d2,
            CategoryDialog.Type.CATEGORY_OPTIONS,
            "uid",
            false,
            null,
            catOptMapper,
            catOptCombMapper,
            schedulerProvider
        )

        whenever(d2.categoryModule().categories().uid("uid").get()) doReturn Single.just(
            Category.builder()
                .uid("uid")
                .displayName("name")
                .build()
        )

        whenever(view.searchSource()) doReturn Observable.just("test")

        presenter.init()
        verify(view).setTitle("name")
    }

    @Test
    fun `Should init for category option combos`() {
        val presenter = CategoryDialogPresenter(
            view,
            d2,
            CategoryDialog.Type.CATEGORY_OPTION_COMBO,
            "uid",
            false,
            null,
            catOptMapper,
            catOptCombMapper,
            schedulerProvider
        )

        whenever(
            d2.categoryModule().categoryCombos().uid("uid").get()
        ) doReturn Single.just(
            CategoryCombo.builder()
                .uid("uid")
                .displayName("name")
                .build()
        )

        whenever(view.searchSource()) doReturn Observable.just("test")

        presenter.init()
        verify(view).setTitle("name")
    }
}
