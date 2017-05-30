package com.udacity.stockhawk.ui;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.udacity.stockhawk.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;

public class DetailActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.lc_detail)
    LineChart mChart;
    @BindView(R.id.srl_detail)
    SwipeRefreshLayout mRefreshLayout;
    private String mSymbol;
    private Stock mStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        mSymbol = getIntent().getStringExtra(MainActivity.DETAIL_DATA);
        mRefreshLayout.setOnRefreshListener(this);
        onRefresh();
    }

    private void drawChart() {
        mChart.setBackgroundColor(Color.LTGRAY);
        try {
            setChartData();
            mChart.animateX(2500);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setChartData() throws IOException {
        ArrayList<String> x = new ArrayList<>();
        ArrayList<Entry> yVals1 = new ArrayList<>();
        ArrayList<Entry> yVals2 = new ArrayList<>();
        ArrayList<Entry> yVals3 = new ArrayList<>();
        for (int i = 0; i < mStock.getHistory().size(); i++) {
            HistoricalQuote quote = mStock.getHistory().get(i);
            Calendar date = quote.getDate();
            x.add(date.toString());

            yVals1.add(new Entry(i, quote.getHigh().floatValue()));
            yVals2.add(new Entry(i, quote.getLow().floatValue()));
            yVals3.add(new Entry(i, quote.getAdjClose().floatValue()));
        }

        LineDataSet set1, set2, set3;
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) mChart.getData().getDataSetByIndex(1);
            set3 = (LineDataSet) mChart.getData().getDataSetByIndex(2);
            set1.setValues(yVals1);
            set2.setValues(yVals2);
            set3.setValues(yVals3);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(yVals1, "High");

            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setCircleColor(Color.WHITE);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);
            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);
//            set1.setDrawValues(false);

            // create a dataset and give it a type
            set2 = new LineDataSet(yVals2, "Low");
            set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set2.setColor(Color.RED);
            set2.setCircleColor(Color.WHITE);
            set2.setLineWidth(2f);
            set2.setCircleRadius(3f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.RED);
            set2.setDrawCircleHole(false);
            set2.setHighLightColor(Color.rgb(244, 117, 117));
            //set2.setFillFormatter(new MyFillFormatter(900f));
//            set2.setDrawValues(false);

            set3 = new LineDataSet(yVals3, "AdjClose");
            set3.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set3.setColor(Color.YELLOW);
            set3.setCircleColor(Color.WHITE);
            set3.setLineWidth(2f);
            set3.setCircleRadius(3f);
            set3.setFillAlpha(65);
            set3.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            set3.setDrawCircleHole(false);
            set3.setHighLightColor(Color.rgb(244, 117, 117));
//            set3.setDrawValues(false);

            // create a data object with the datasets
            LineData data = new LineData(set1, set2, set3);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            mChart.setData(data);
        }
    }

    @Override
    public void onRefresh() {
        if (mSymbol != null) {
            new StockTask().execute(mSymbol);
        } else {
            Toast.makeText(this, R.string.stock_null, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class StockTask extends AsyncTask<String, Void, Stock> {

        @Override
        protected void onPreExecute() {
            mRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Stock doInBackground(String... params) {
            Stock stock = null;
            try {
                stock = YahooFinance.get(params[0], true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stock;
        }

        @Override
        protected void onPostExecute(Stock stock) {
            mRefreshLayout.setRefreshing(false);
            mStock = stock;
            if (mStock != null) {
                drawChart();
            }
        }
    }
}
