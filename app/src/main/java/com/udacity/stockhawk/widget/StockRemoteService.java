package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created on 2017/6/1
 *
 * @author saker
 */

public class StockRemoteService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteFactory();
    }

    private class StockRemoteFactory implements RemoteViewsFactory {

        private Cursor mCursor;
        private Context mContext;

        @Override
        public void onCreate() {
            mContext = StockRemoteService.this;
        }

        @Override
        public void onDataSetChanged() {
            if (mCursor != null) {
                mCursor.close();
            }
            final long identityToken = Binder.clearCallingIdentity();
            mCursor = getContentResolver().query(Contract.Quote.URI, null, null, null, null);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }

        @Override
        public int getCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION ||
                    mCursor == null || !mCursor.moveToPosition(position)) {
                return null;
            }
            // get data
            String symbol = mCursor.getString(Contract.Quote.POSITION_SYMBOL);
            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            String price = dollarFormat.format(mCursor.getFloat(Contract.Quote.POSITION_PRICE));
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");
            String percentage = percentageFormat.format(mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE));
            // update ui
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.list_item_widget);
            remoteViews.setTextViewText(R.id.symbol, symbol);
            remoteViews.setTextViewText(R.id.price, price);
//            if (PrefUtils.getDisplayMode(mContext)
//                    .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            remoteViews.setTextViewText(R.id.change, change);
//            } else {
//                remoteViews.setTextViewText(R.id.change, percentage);
//            }
            if (rawAbsoluteChange > 0) {
                remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }
            // add click event(fill in)
            Intent intent = new Intent();
            intent.putExtra(MainActivity.DETAIL_DATA, symbol);
            remoteViews.setOnClickFillInIntent(R.id.list_item_widget, intent);
            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (mCursor.moveToPosition(position))
                return mCursor.getLong(Contract.Quote.POSITION_ID);
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
