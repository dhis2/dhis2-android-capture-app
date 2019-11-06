package org.dhis2.usecases.datasets.dataSetTable

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract
import org.dhis2.usescases.datasets.dataSetTable.DataSetTablePresenter
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableRepository
import org.hisp.dhis.android.core.common.State
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DataSetTablePresenterTest {

    private lateinit var presenter: DataSetTablePresenter

    private val view: DataSetTableContract.View = mock()
    private val repository: DataSetTableRepository = mock()
    private val scheduler = TrampolineSchedulerProvider()

    @Before
    fun setUp() {
        presenter = DataSetTablePresenter(view, repository, scheduler)
    }

    @Test
    fun `Should go back when bakc button is clicked`(){
        presenter.onBackClick()

        verify(view).back()
    }

    @Test
    fun `Should show syncDialog when button is clicked`(){
        presenter.onSyncClick()

        verify(view).showSyncDialog()
    }

    @Test
    fun `Should dispose of all disposables`(){
        presenter.onDettach()

        val disposableSize = presenter.disposable.size()

        Assert.assertTrue(disposableSize == 0)
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }

    @Test
    fun `Should go on selected table`() {
        val table = 1

        presenter.onClickSelectTable(table)

        verify(view).goToTable(table)
    }

    @Test
    fun `Should return the CategoryOptionCombo from list`() {
        val categoryOptions = listOf("option_1", "option_2", "option_3")
        val categoryOptionCombo = "category_option_combo"
        whenever(
            repository.getCatOptComboFromOptionList(categoryOptions)
        ) doReturn categoryOptionCombo

        val result = presenter.getCatOptComboFromOptionList(categoryOptions)

        assert(result == categoryOptionCombo)
    }

    @Test
    fun `Should set the dataset state`() {
        val state = State.SYNCED
        whenever(repository.dataSetState()) doReturn Flowable.just(state)

        presenter.updateState()

        verify(view).setDataSetState(state)
    }
}