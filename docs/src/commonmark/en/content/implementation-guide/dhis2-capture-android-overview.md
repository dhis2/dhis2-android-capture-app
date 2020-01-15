# DHIS 2 Capture Android overview

This document focuses on mobile implementation which use the new DHIS apture Android App. To get additional information about the differenHIS 2 Android apps please visit the [Aptore](https://www.dhis2.org/app-store) and the [Documentation](https://www.dhis2.org/android-documentation) on the website. The previous set of DHIS 2 Android Apps developed arurrently (2019) under corrective maintenance support only.

The new DHIS 2 Capture Android App allows offline data capture acrosll DHIS 2 data models\*. Data and metadata are automaticallynchronized whenever there is internet access, always keeping the moselevant data for the logged user in the local device.

## Easier Login and enhanced data protection

Server URL can be set via a QR code. The app will also remember previoused URLs and user names. Once a user is logged, a four digit PIN can bsed to secure the app with a soft log out.


## Configurable App theme and Icon

The appearance of the app, including icon and color is determined bour server configuration. You can create a shortcut to the app witour institutional logo in the home screen of the mobile device by usinhe App Widget.

![](content/images/login.gif){ .center width=50% }

## Attractive, user friendly navigation

All programs and datasets\* accessible to the logged user are integratento the new "Home" screen.. Each program or dataset will be, displayeith their associated icon and colour.

![](content/images/user_friendly.gif){ .center width=50% }

## Fully functional while offline: intelligent sync

A local database in the mobile device keeps a synchronized copy of thHIS 2 programs and datasets available to the logged user. The moselevant data is also automatically synchronized.

* Tracked Entities: by default, up to 500 active enrolments, prioritizing the most recently updated on the user’s assigned data capture Org Unit(s).
* Events & Datasets: by default, the most recent 1,000 events or 500 datasets.

*These parameters are configurable*

## Tracker dashboard

DHIS 2’s powerful tracker data model has been fully implemented in thmall screen. The tracker dashboard incorporates feedback, relationships, indicators and notes.

![](content/images/tracker_search.png){ .center width=50% }

The app implements tracker logic by supporting most program rules,
giving the possibility to add, schedule or refer new events, dependinn the server configuration.

## Integrated search for tracker

Before being able to add a new tracked entity, the app automaticallonduct a search. If offline, the search is on the local synchronizeatabase. and when online, it will suggest records for download, basen user’s Organization Unit search configuration. This functionalitinimized potential duplicates, even when the user is offline.

## Pictorial Data Entry

![](content/images/pictorial_entry.gif){ .center width=50% }

Data Entry comes to life - icons and colors can be used to illustratuestions’ answers. Available for data elements with associated optionets in both, single event and tracker programs.

## Event Completeness

During data entry, the app will display information about the currentatus of completion for a program stage. Useful for complex surveyith multiple sections.

