package org.dhis2.android.rtsm.data

import com.github.javafaker.Faker
import org.hisp.dhis.android.core.arch.helpers.UidGenerator
import org.hisp.dhis.android.core.arch.helpers.UidGeneratorImpl
import org.hisp.dhis.android.core.option.Option

object DestinationFactory {
    private val uidGenerator: UidGenerator = UidGeneratorImpl()
    private val faker: Faker = Faker()

    fun create(id: Long): Option {
        val name = faker.address().streetName()

        return Option
            .builder()
            .uid(uidGenerator.generate())
            .name(name)
            .displayName(name)
            .build()
    }

    fun getListOf(num: Int): List<Option> = (1..num).map { create(it.toLong()) }
}
