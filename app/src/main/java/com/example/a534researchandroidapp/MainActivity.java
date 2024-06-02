package com.example.a534researchandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {
    // The time (in ms) between ticks when running the btnStart function.
    private final int TIME_INTERVAL = 400;
    private final int MAX_RANDOM_INT = 1000;
    private final int MIN_RANDOM_INT = 1;


    private Button btnRandomInt, btnStart, btnStop, btnClear, btnAiReport;
    private TextView txtInitialData;
    private LineChart lChartHistoric;


    private int xValCount = 0;
    private List<Entry> historicalEntries = new ArrayList<Entry>();

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private boolean handlerIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRandomInt = (Button) findViewById(R.id.btn_random_int);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnAiReport = (Button) findViewById(R.id.btn_ai_report);
        txtInitialData = (TextView) findViewById(R.id.txt_initial_data);
        lChartHistoric = (LineChart) findViewById(R.id.line_chart_historic);

        // Similar to a TimerTask for java.util.Timer; gets called ever TIME_INTERVAL.
        runnable = new Runnable() {
            @Override
            public void run() {
                addRandomEntryHistorical();
                updateLChartHistorical();
                handler.postDelayed(this, TIME_INTERVAL); // Schedule the runnable to run again after 1 second
            }
        };

        btnRandomInt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRandomEntryHistorical();
                updateLChartHistorical();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!handlerIsRunning) {
                    handlerIsRunning = !handlerIsRunning;
                    handler.post(runnable);
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                handlerIsRunning = false;
            }
        });

        btnClear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                historicalEntries.clear();
                updateLChartHistorical();
                return false;
            }
        });

        btnAiReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Entry> sortedHistoricalEntries = historicalEntries.stream().sorted((p1, p2) -> Float.compare(p2.getY(), p1.getY())).collect(Collectors.toList());
                String strOut;
                if (!sortedHistoricalEntries.isEmpty()) {
                    strOut = String.format("Max: (%.0f, %.0f)!\n", sortedHistoricalEntries.get(0).getX(), sortedHistoricalEntries.get(0).getY());
                    strOut += String.format("Y-val Mean: %.2f\n", sortedHistoricalEntries.stream().mapToInt(p1 -> (int) p1.getY()).average().orElse(0));
                    if (sortedHistoricalEntries.size() % 2 == 0) {
                        float mid1 = sortedHistoricalEntries.get((int) (sortedHistoricalEntries.size() / 2)).getY();
                        float mid2 = sortedHistoricalEntries.get((int) (sortedHistoricalEntries.size() / 2) - 1).getY();
                        strOut += String.format("Y-val Median: %.1f\n",  (mid1 + mid2) / 2);
                    }  else {
                        strOut += String.format("Y-val Median: %.1f\n", sortedHistoricalEntries.get((int) (sortedHistoricalEntries.size() / 2)).getY());
                    }
                } else {
                    strOut = "No data to interpret...";
                }
                txtInitialData.setText(strOut);
            }
        });
    }

    public void addEntryHistorical(int toBeAdded) {
        historicalEntries.add(new Entry(xValCount++, toBeAdded));
    }

    public void updateLChartHistorical() {
        // .*DataSet puts your data together, which can then be formatted before display.
        LineDataSet dataSet1 = new LineDataSet(historicalEntries, "");
        dataSet1.setColor(Color.rgb(155, 0, 0));
        dataSet1.setCircleColor(Color.rgb(155,0,0));
        dataSet1.setDrawFilled(true);
        dataSet1.setFillColor(Color.rgb(100, 0, 0));
        dataSet1.setDrawValues(false);

        // .*Data is an object for even further customizing your specific graph output.
        LineData lineData = new LineData(dataSet1);
        // One could instead combine two separate datasets into one ArrayList then feed it into the LineData object instead.
//        ArrayList dataSetFinal = new ArrayList<>();
//        dataSetFinal.add(dataSet1);
//        dataSetFinal.add(dataSet2);
//        LineData lineData = new LineData(dataSetFinal);

        // x-axis customizing
        lChartHistoric.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lChartHistoric.getXAxis().setTextSize(15); // The float represents pixel density, within [6f, 24f]
        lChartHistoric.getXAxis().setGranularity(1); // Sets the minimum step when zooming in
        // y-axis customizing
        lChartHistoric.getAxisRight().setDrawLabels(false); // Disables the desired y-axis
        lChartHistoric.getAxisLeft().setGranularity(1);
        lChartHistoric.getAxisLeft().setLabelCount(10);
        lChartHistoric.getAxisLeft().setTextSize(15);


        // Finalizes and displays your data!
        lChartHistoric.setData(lineData);
        lChartHistoric.invalidate(); // refreshes the chart
    }

    public void addRandomEntryHistorical() {
        // Generates a int in the range [1, 10]
        int rand = (int) (Math.random() * MAX_RANDOM_INT + MIN_RANDOM_INT);

//        txtInitialData.setText(Integer.toString(rand));
        addEntryHistorical(rand);
    }
}