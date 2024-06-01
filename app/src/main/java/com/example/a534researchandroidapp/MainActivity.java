package com.example.a534researchandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
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

        // One can combine two separate datasets into one ArrayList then feed it into the LineData object instead.
//        ArrayList dataSetFinal = new ArrayList<>();
//        dataSetFinal.add(dataSet1);
//        dataSetFinal.add(dataSet2);
//        LineData lineData = new LineData(dataSetFinal);

        // .*Data is an object for even further customizing your specific graph output.
        LineData lineData = new LineData(dataSet1);
        lineChartHistoric.getXAxis().setTextSize(200);
        lineChartHistoric.getXAxis().setGranularity(1); // Sets the 'step' of the x-axis sort of
        lineChartHistoric.getAxisLeft().setDrawLabels(false); // Disables the left y-axis

        // Finalizes and displays your data!
        lineChartHistoric.setData(lineData);
        lineChartHistoric.invalidate(); // refresh
    }
}