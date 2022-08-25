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

        return Option.builder()
            .id(id)
            .uid(uidGenerator.generate())
            .name(name)
            .displayName(name).build()
    }

    fun getListOf(num: Int): List<Option> {
        return (1..num).map { create(it.toLong()) }
    }
}