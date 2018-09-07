package org.dhis2.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.dhis2.R
import org.dhis2.usescases.splash.SplashActivity
import org.dhis2.utils.Constants


/**
 * Implementation of App Widget functionality.
 */
class DhisCustomLauncher : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        val remoteViews = RemoteViews(context.packageName, R.layout.dhis_custom_launcher)
        val configIntent = Intent(context, SplashActivity::class.java)

        val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)

        remoteViews.setOnClickPendingIntent(R.id.appwidget_image, configPendingIntent)
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            val prefs = context.getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE)
            val widgetImage = prefs.getString("FLAG", null)
            val icon = context.resources.getIdentifier(widgetImage, "drawable", context.packageName)
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.dhis_custom_launcher)

            views.setImageViewResource(R.id.appwidget_image, icon)
            views.setOnClickPendingIntent(R.id.appwidget_image, getPendingIntent(context))
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, SplashActivity::class.java)
            return PendingIntent.getActivity(context, 0, intent, 0)
        }
    }

}

