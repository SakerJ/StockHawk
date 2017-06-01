package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

//        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Click the widget's title
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        //update ListView, add click event template
        views.setRemoteAdapter(R.id.lv_widget, new Intent(context, StockRemoteService.class));
        views.setEmptyView(R.id.lv_widget, R.id.tv_widget_empty);
        Intent clickIntent = new Intent(context, DetailActivity.class);
        PendingIntent clickPendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.lv_widget, clickPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // update ListView's data
        if(QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, StockWidget.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.lv_widget);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

