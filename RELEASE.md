# New functionality and web parity

## Login with OAuth and two-factor authentication

Version 3.5 adds login with OAuth. If the server has OAuth enabled, users log in on the login page of the server, in the browser. The server can ask for a code from an authenticator app. Users with an OAuth account can turn two-factor authentication on or off in **Settings**.

Users with an OAuth or OpenID Connect account must create a 4-digit offline code before they can use the app. The code unlocks the account without a connection. If the device has a fingerprint or face sensor, users can also unlock the account with it. After 3 wrong attempts, the app locks the login for 60 seconds.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-6574)

## Customized tracker terminology

Administrators can customize more of the terminology that the app uses in a program. The app shows these terms in filters, lists, menus, and messages. The new terms are:

- Program stage
- Follow-up
- Relationship (one term for singular and plural)
- Organisation unit (one term for singular and plural)
- The plural of enrollment and the plural of event
- Tracked entity type (plural only)

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-4731)

## Program indicator disaggregation

In DHIS2 42 and later, program indicators can be split by category, for example by gender or by age group. The app shows these values in the same analytics tables as before. Administrators do not need to configure anything in the app.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-7277)

## Image quality control

In the Android Settings Web App, administrators can choose the image fields whose pictures the app sends in their original quality. If the app did not download a picture, the field will showan image icon with a line across it.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-7606)

## Working lists

Working lists can now filter by empty and not empty values.
[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-7701)

# User experience
## Organisation unit selector

If several organisation units have the same name and the list does not show their parent, the selector adds a tag next to each of them. The tag will show the name of the parent it belongs to t.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-7609)

# Performance and maintenance

## SDK modernization

The SDK continues to move from Java models to Kotlin data classes, and to remove its Android dependencies. This is some ground-laying work to prepare for a future migration to a multiplatform SDK. These changes are transparent for the user. .

[Jira](https://dhis2.atlassian.net/browse/ANDROSDK-2225) | [Jira](https://dhis2.atlassian.net/browse/ANDROSDK-1586)

## Bug fixes

See the [full list of fixed bugs in Jira](https://dhis2.atlassian.net/projects/ANDROAPP/versions/12052/tab/release-report-all-issues).

