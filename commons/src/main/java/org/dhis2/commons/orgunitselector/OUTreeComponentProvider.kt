package org.dhis2.commons.orgunitselector

interface OUTreeComponentProvider {
    fun provideOUTreeComponent(module: OUTreeModule): OUTreeComponent?
}
