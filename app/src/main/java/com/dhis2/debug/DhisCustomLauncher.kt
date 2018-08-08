package com.dhis2.debug

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.dhis2.R

/**
 * Implementation of App Widget functionality.
 */
class DhisCustomLauncher : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
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

            val prefs = context.getSharedPreferences("com.dhis2", Context.MODE_PRIVATE)
            val widgetImage = prefs.getString("FLAG",null)
            val icon = context.resources.getIdentifier(widgetImage,"drawable",context.packageName)
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.dhis_custom_launcher)

            views.setImageViewResource(R.id.appwidget_image, icon)



            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

