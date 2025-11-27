package org.dhis2.mobile.login.accounts.data.credentials

data class TestingCredentials(
    val server: String,
    var username: String,
    var password: String,
)

val defaultTestingCredentials =
    listOf(
        TestingCredentials(
            server = "https://android.im.dhis2.org/current",
            username = "android",
            password = "Android123",
        ),
        TestingCredentials(
            server = "https://android.im.dhis2.org/dev",
            username = "android",
            password = "Android123",
        ),
    )

val trainingTestingCredentials =
    listOf(
        TestingCredentials(
            server = "https://play.dhis2.org/demo",
            username = "android",
            password = "Android123",
        ),
    )
