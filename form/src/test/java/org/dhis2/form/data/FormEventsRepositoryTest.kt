package org.dhis2.form.data

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class FormEventsRepositoryTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val recordUid = "ABCD"
    private val formRepositoryImpl: FormRepositoryImpl = mock()

    private lateinit var formEventsRepository: FormEventsRepository

    @Before
    fun setUp() {
        formEventsRepository = FormEventsRepository(
            formRepositoryImpl,
            d2,
            recordUid
        )
    }

    /*@Test
    fun `test`() {
        assertThat(formEventsRepository.s)
    }*/
}