/*
 * Copyright (c) 2004 - 2019, University of Oslo
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.utils.optionset

import androidx.paging.ItemKeyedDataSource
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.option.OptionGroup
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class OptionSetPresenterTest {

    private lateinit var presenter: OptionSetPresenter
    private val testSchedulers: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val view: OptionSetView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setup() {
        presenter = OptionSetPresenter(view, d2, testSchedulers)
    }

    @Test
    fun `Should show full list of options`() {
        val dataSource = OptionsDataSource()
        whenever(d2.optionModule().options().byOptionSetUid().eq("optionSet")) doReturn mock()
        whenever(d2.optionModule().optionGroups().withOptions().uid("optionGroup")) doReturn mock()
        whenever(
            d2.optionModule().optionGroups().withOptions().uid("optionGroup").blockingGet()
        ) doReturn mockOptionGroupList()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSet").dataSource
        ) doReturn dataSource

        whenever(view.searchSource()) doReturn Observable.just("" as CharSequence)

        presenter.init(getTestSpinnerModel())

        testSchedulers.io().advanceTimeBy(500, TimeUnit.MILLISECONDS)
        testSchedulers.io().triggerActions()
        testSchedulers.ui().triggerActions()
        verify(view, times(1)).setLiveData(any())
    }

    @Test
    fun `Should return the number of options available`() {
        whenever(d2.optionModule().options().byOptionSetUid().eq("optionSet")) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSet").blockingCount()
        ) doReturn 5

        val count = presenter.getCount("optionSet")

        assertTrue(count == 5)
    }

    @Test
    fun `Should clear the disposables onDetach()`() {
        presenter.onDetach()
        val disposableSize = presenter.disposable.size()
        assertTrue(disposableSize == 0)
    }

    private fun mockOptionGroupList(): OptionGroup {
        return OptionGroup.builder()
            .uid("optionGroup")
            .optionSet(ObjectWithUid.create("optionSet"))
            .options(mockOptionList())
            .build()
    }

    private fun mockOptionList(): List<ObjectWithUid> {
        return listOf(
            ObjectWithUid.create("option1"),
            ObjectWithUid.create("option2"),
            ObjectWithUid.create("option3"),
            ObjectWithUid.create("option4"),
            ObjectWithUid.create("option5")
        )
    }

    private fun getTestSpinnerModel(): SpinnerViewModel {
        return SpinnerViewModel.create(
            "id", "label", "hint",
            false,
            "optionSet",
            "value",
            "section",
            true,
            "description",
            10,
            ObjectStyle.builder().build()
        )
    }

    inner class OptionsDataSource : ItemKeyedDataSource<Option, Option>() {
        override fun loadInitial(p: LoadInitialParams<Option>, cb: LoadInitialCallback<Option>) {
            val options = mockSortedOptions()
            cb.onResult(options)
        }

        override fun loadAfter(params: LoadParams<Option>, callback: LoadCallback<Option>) {}

        override fun loadBefore(params: LoadParams<Option>, callback: LoadCallback<Option>) {}

        override fun getKey(item: Option) = item

        private fun mockSortedOptions(): List<Option> {
            val options = mutableListOf<Option>()
            val index = 1
            while (index <= 5) {
                options.add(Option.builder().uid("option$index").sortOrder(index).build())
                index + 1
            }
            return options
        }
    }
}
