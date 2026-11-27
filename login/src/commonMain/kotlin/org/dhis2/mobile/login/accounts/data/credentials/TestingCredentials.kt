package org.dhis2.mobile.login.accounts.data.credentials

data class TestingCredentials(
    val server: String,
    var username: String? = null,
    var password: String? = null,
)

val defaultTestingCredentials =
    listOf(
        TestingCredentials(
            server = "https://android.im.dhis2.org/current",
        ),
        TestingCredentials(
            server = "https://android.im.dhis2.org/dev",
        ),
    )

val trainingTestingCredentials =
    listOf(
        TestingCredentials(
            server = "https://play.dhis2.org/demo",
        ),
    )
