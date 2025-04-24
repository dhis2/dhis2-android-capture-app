package org.dhis2.usescases.teiDashboard

interface TeiDashboardMenuCustomActionsManager {
  /**
   * This method is used to send SMS to the TEI.
   * @param teiUid The UID of the TEI to whom the SMS will be sent.
   */
  fun sendSms(teiUid: String?)
  /**
   * This method is called when the manager is destroyed.
   */
  fun onDestroy()
}