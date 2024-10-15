package org.dhis2.android.rtsm.data

import com.github.javafaker.Faker
import org.hisp.dhis.android.core.arch.helpers.UidGenerator
import org.hisp.dhis.android.core.arch.helpers.UidGeneratorImpl
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualization
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup

object GroupAnalyticsFactory {
    private val uidGenerator: UidGenerator = UidGeneratorImpl()
    private val faker: Faker = Faker()

    fun create(id: Long): AnalyticsDhisVisualizationsGroup {
        val name = faker.address().streetName()

        return AnalyticsDhisVisualizationsGroup.builder()
            .id(uidGenerator.generate())
            .name(name)
            .visualizations(VisualizationsFactory.getListOf(3))
            .build()
    }

    fun getListOf(num: Int): List<AnalyticsDhisVisualizationsGroup> {
        return (1..num).map { create(it.toLong()) }
    }
}

object VisualizationsFactory {
    private val uidGenerator: UidGenerator = UidGeneratorImpl()
    private val faker: Faker = Faker()

    fun create(id: Long): AnalyticsDhisVisualization {
        val name = faker.address().streetName()

        return AnalyticsDhisVisualization.builder()
            .id(id)
            .name(name)
            .uid(uidGenerator.generate())
            .build()
    }

    fun getListOf(num: Int): List<AnalyticsDhisVisualization> {
        return (1..num).map { create(it.toLong()) }
    }
}
