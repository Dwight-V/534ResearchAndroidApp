package com.example.a534researchandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Button btnRandomInt;
    private TextView txtInitialData;
    private LineChart lineChartHistoric;


    int xValCount = 0;
    List<Entry> lChartHistoricEntries = new ArrayList<Entry>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRandomInt = (Button) findViewById(R.id.btn_random_int);
        txtInitialData = (TextView) findViewById(R.id.txt_initial_data);
        lineChartHistoric = (LineChart) findViewById(R.id.lchart_historic);


        btnRandomInt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generates a int in the range [1, 10]
                int randInt = (int) (Math.random() * 10 + 1);

                txtInitialData.setText(Integer.toString(randInt));
                addEntry(randInt);
                updateLChartHistorical();
            }
        });
    }

    public void addEntry(int toBeAdded) {
        lChartHistoricEntries.add(new Entry(xValCount++, toBeAdded));
    }

    public void updateLChartHistorical() {
        // .*DataSet puts your data together, which can then be formatted before display.
        LineDataSet dataSet1 = new LineDataSet(lChartHistoricEntries, "");
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
        lineChartHistoric.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartHistoric.getXAxis().setTextSize(15); // The float represents pixel density, within [6f, 24f]
        lineChartHistoric.getXAxis().setGranularity(1); // Sets the minimum step when zooming in
        // y-axis customizing
        lineChartHistoric.getAxisRight().setDrawLabels(false); // Disables the desired y-axis
        lineChartHistoric.getAxisLeft().setGranularity(1);
        lineChartHistoric.getAxisLeft().setLabelCount(10);
        lineChartHistoric.getAxisLeft().setTextSize(15);


        // Finalizes and displays your data!
        lineChartHistoric.setData(lineData);
        lineChartHistoric.invalidate(); // refreshes the chart
    }
}