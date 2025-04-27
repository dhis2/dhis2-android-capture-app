package org.dhis2.usescases.teiDashboard

import android.view.View

/**
 * Interface to manage custom actions for the TEI (Tracked Entity Instance) dashboard menu.
 */
interface TeiDashboardMenuCustomActionsManager {

  /**
   * Sends an SMS related to a specific Tracked Entity Instance (TEI).
   *
   * @param teiUid The unique identifier of the TEI. Can be null.
   * @param parentView The parent view from which the SMS action is triggered.
   */
  fun sendSms(teiUid: String?, parentView : View)
  /**
   * This method is called when the manager is destroyed.
   */
  fun onDestroy()
}