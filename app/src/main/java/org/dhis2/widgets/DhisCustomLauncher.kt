package org.dhis2.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.text.TextUtils.isEmpty
import android.widget.RemoteViews
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.usescases.splash.SplashActivity

class DhisCustomLauncher : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
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

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            var widgetImage = ""
            if (context.app().serverComponent != null) {
                val d2 = context.app().serverComponent?.userManager()?.d2
                if (d2 != null) {
                    val isLoggedIn = d2.userModule().isLogged().blockingGet()
                    widgetImage = if (isLoggedIn) {
                        d2.settingModule()?.systemSetting()?.flag()?.blockingGet()?.value() ?: ""
                    } else {
                        ""
                    }
                }
            }

            val icon =
                if (!isEmpty(widgetImage)) {
                    context.resources.getIdentifier(widgetImage, "drawable", context.packageName)
                } else {
                    R.drawable.ic_dhis
                }

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.dhis_custom_launcher)

            if (icon != 0) {
                views.setImageViewResource(R.id.appwidget_image, icon)
            }
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
