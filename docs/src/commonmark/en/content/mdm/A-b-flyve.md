# Annex B - MDM PoC: Flyve MDM

This annex presents the outcome of the tested MDM: [https://www.flyve-mdm.com/](https://www.flyve-mdm.com/)

Flyve MDM is based on [GLPI](https://glpi-project.org/), so it needs GLPI to be working as a subsystem before Flyve MDM can be used. GLPI _is an open source IT Asset Management, issue tracking system and service desk system. This software is written in PHP and distributed under the GNU General Public License._


## Installation & Usage

It is easy to test on premises because they provide docker containers which allow quick testing.

A demo on the cloud version can also be requested.

GLPI might look a bit overwhelming at the beginning, but it can be a very big advantage if a solution like this is already in place.

App is available on F-Droid so can ease the installation or testing process.

The list of supported features can be found here: [http://flyve.org/android-mdm-agent/howtos/policies](http://flyve.org/android-mdm-agent/howtos/policies)


## Issues

Does not support KIOSK mode

The MDM Dashboard is a much nicer console but still relies on GLPI underneath.


## Conclusion

Might not be worth it depending on the set-up, as the MDM management console and GLPI might be overwhelming if no previous experience exists in this software. Also, KIOSK mode unavailability might be a dealbreaker.

It is Open Source so the costs can be reduced significantly if self-hosted; maybe ideal for really small implementations or to test the capabilities of an MDM before scaling up.
