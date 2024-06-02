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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {
    // The time (in ms) between ticks when running the btnStart function.
    private final int TIME_INTERVAL = 400;
    private final int MAX_RANDOM_INT = 1000;
    private final int MIN_RANDOM_INT = 1;
    // What the graph puts as the key for random data.
    private final String DATA_LABEL = "Data";


    private Button btnRandomInt, btnStart, btnStop, btnClear, btnAiReport;
    private TextView txtInitialData;
    private LineChart lChartHistoric;


    private int xValCount = 0;
    // Holds the raw data for the graph. This data is then converted to display in the graph.
    private ArrayList<Entry> historicalEntries = new ArrayList<Entry>();
    // Keeps track of the total mean up to every point in the graph.
    private ArrayList<Entry> historicalEntriesMean = new ArrayList<>();
    // Used in btnStart's loop.
    private Handler handler = new Handler(Looper.getMainLooper());
    // Used in btnStart's loop.
    private Runnable runnable;
    // Used to prevent double-clicking btnStart for twice the speed.
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
                updateLChartHistorical(createLineDataSet(historicalEntries, DATA_LABEL, 200, 0, 0));
                handler.postDelayed(this, TIME_INTERVAL); // Schedule the runnable to run again after 1 second
            }
        };

        btnRandomInt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRandomEntryHistorical();
                updateLChartHistorical(createLineDataSet(historicalEntries, DATA_LABEL, 200, 0, 0));
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
                historicalEntriesMean.clear();
                xValCount = 0;
                updateLChartHistorical(createLineDataSet(historicalEntries, DATA_LABEL, 200, 0, 0));
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
                    strOut += String.format("Y-val Mean: %.2f\n", historicalEntriesMean.get(historicalEntriesMean.size() - 1).getY());
                    if (sortedHistoricalEntries.size() % 2 == 0) {
                        float mid1 = sortedHistoricalEntries.get((int) (sortedHistoricalEntries.size() / 2)).getY();
                        float mid2 = sortedHistoricalEntries.get((int) (sortedHistoricalEntries.size() / 2) - 1).getY();
                        strOut += String.format("Y-val Median: %.1f\n",  (mid1 + mid2) / 2);
                    }  else {
                        strOut += String.format("Y-val Median: %.1f\n", sortedHistoricalEntries.get((int) (sortedHistoricalEntries.size() / 2)).getY());
                    }
                    updateLChartHistorical(createLineDataSet(historicalEntriesMean, "Mean to date", 0, 200, 0),
                            createLineDataSet(historicalEntries, DATA_LABEL, 200, 0, 0));

                } else {
                    strOut = "No data to interpret...";
                }
                txtInitialData.setText(strOut);
            }
        });
    }

    public void addEntryHistorical(int toBeAdded) {
        historicalEntries.add(new Entry(xValCount++, toBeAdded));
        historicalEntriesMean.add(new Entry(xValCount - 1, (float) historicalEntries.stream().mapToInt(p1 -> (int) p1.getY()).average().orElse(0)));
    }

    public void updateLChartHistorical(LineDataSet... dataSets) {
        if (dataSets.length == 0) {
            Toast.makeText(this, "Empty...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Needs to be Raw declaration and not ArrayList<Entry>, because cannot cast to (ILineData) set when instantiating lineData
        ArrayList dataSetFinal = new ArrayList<>();
        dataSetFinal.addAll(Arrays.asList(dataSets));

        LineData lineData = new LineData(dataSetFinal);

        // x-axis customizing
        lChartHistoric.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lChartHistoric.getXAxis().setTextSize(15); // The float represents pixel density, within [6f, 24f]
        lChartHistoric.getXAxis().setGranularity(1); // Sets the minimum step when zooming in
        lChartHistoric.getXAxis().setAxisMinimum(0);
        // y-axis customizing
        lChartHistoric.getAxisRight().setDrawLabels(false); // Disables the desired y-axis
        lChartHistoric.getAxisLeft().setGranularity(1);
        lChartHistoric.getAxisLeft().setLabelCount(10);
        lChartHistoric.getAxisLeft().setAxisMinimum(0);
        lChartHistoric.getAxisLeft().setAxisMaximum(MAX_RANDOM_INT);
        lChartHistoric.getAxisLeft().setTextSize(15);


        // Finalizes and displays your data!
        lChartHistoric.setData(lineData);
        lChartHistoric.invalidate(); // refreshes the chart
    }

    public LineDataSet createLineDataSet(ArrayList<Entry> entries, String strLabel, int red, int blue, int green) {
        // .*DataSet puts your data together, which can then be formatted before display.
        LineDataSet dataSet1 = new LineDataSet(entries, strLabel);
        dataSet1.setColor(Color.rgb(red, blue, green));
        dataSet1.setCircleColor(Color.rgb(red, blue, green));
//        dataSet1.setDrawFilled(true);
//        dataSet1.setFillColor(Color.rgb(150, 0, 0));
        dataSet1.setDrawValues(false);
        return dataSet1;
    }

    public void addRandomEntryHistorical() {
        // Generates a int in the range [1, 10]
        int rand = (int) (Math.random() * MAX_RANDOM_INT + MIN_RANDOM_INT);

//        txtInitialData.setText(Integer.toString(rand));
        addEntryHistorical(rand);
    }
}