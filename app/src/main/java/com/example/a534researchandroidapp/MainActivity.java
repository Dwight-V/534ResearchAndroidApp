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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRandomInt = (Button) findViewById(R.id.btn_random_int);
        txtInitialData = (TextView) findViewById(R.id.txt_initial_data);
        lineChartHistoric = (LineChart) findViewById(R.id.lchart_historic);


        //region
        int[][] dataObjects = {{0, 1}, {1, 2}, {2, 2}, {3, 3}, {4, 3}};

        List<Entry> entries = new ArrayList<Entry>();

        // Used to convert your data into the correct format.
        for (int[] data : dataObjects) {
            // turn your data into Entry objects
            entries.add(new Entry(data[0], data[1]));
        }

        int[][] dataObjects2 = {{0, 3}, {1, 2}, {2, 1}, {3, 0}, {4, 1}};

        List<Entry> entries2 = new ArrayList<Entry>();

        // Used to convert your data into the correct format.
        for (int[] data : dataObjects2) {
            // turn your data into Entry objects
            entries2.add(new Entry(data[0], data[1]));
        }

        // *DataSet puts your data together, which can then be formatted before display.
        LineDataSet dataSet1 = new LineDataSet(entries, "Test Label");
        dataSet1.setColor(Color.rgb(155, 0, 0));
        dataSet1.setCircleColor(Color.rgb(155,0,0));
        dataSet1.setDrawFilled(true);
        dataSet1.setFillColor(Color.rgb(100, 0, 0));

        LineDataSet dataSet2 = new LineDataSet(entries2, "Test 2 Label");
        dataSet2.setColor(Color.rgb(0, 155, 0));
        dataSet2.setCircleColor(Color.rgb(0,0,155));
        dataSet2.setDrawFilled(true);
        dataSet2.setDrawCircleHole(false);
        dataSet2.setFillColor(Color.rgb(0, 80, 0));

        // Combines the two separate datasets into one ArrayList to feed into the LineData object.
        ArrayList dataSetFinal = new ArrayList<>();
        dataSetFinal.add(dataSet1);
        dataSetFinal.add(dataSet2);

        // *Data is an object for even further customizing your specific graph output.
        LineData lineData = new LineData(dataSetFinal);
        lineChartHistoric.getXAxis().setTextSize(200);
        lineChartHistoric.getXAxis().setGranularity(1); // Sets the 'step' of the x-axis sort of
        lineChartHistoric.getAxisLeft().setDrawLabels(false); // Disables the left y-axis

        lineChartHistoric.setData(lineData);
        lineChartHistoric.invalidate(); // refresh
        //endregion


        btnRandomInt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generates a int in the range [1, 10]
                int randInt = (int) (Math.random() * 10 + 1);
                txtInitialData.setText(Integer.toString(randInt));
            }
        });
    }

    // Creates and displays the line chart. Used to unify the look of all line charts in our app.
//    public void createLineChart(LineChart lChart, int[][] data, String xAxisName, String yAxisName) {
//        List<Entry> entries = new ArrayList<Entry>();
//
//        // Used to convert your data into the correct format.
//        for (int[] i : data) {
//            // turn your data into Entry objects
//            entries.add(new Entry(i[0], i[1]));
//        }
//
//        int[][] dataObjects2 = {{0, 3}, {1, 2}, {2, 1}, {3, 0}, {4, 1}};
//
//        List<Entry> entries2 = new ArrayList<Entry>();
//
//        // Used to convert your data into the correct format.
//        for (int[] data : dataObjects2) {
//            // turn your data into Entry objects
//            entries2.add(new Entry(data[0], data[1]));
//        }
//
//        // *DataSet puts your data together, which can then be formatted before display.
//        LineDataSet dataSet1 = new LineDataSet(entries, "Test Label");
//        dataSet1.setColor(Color.rgb(155, 0, 0));
//        dataSet1.setCircleColor(Color.rgb(155,0,0));
//        dataSet1.setDrawFilled(true);
//        dataSet1.setFillColor(Color.rgb(100, 0, 0));
//
//        LineDataSet dataSet2 = new LineDataSet(entries2, "Test 2 Label");
//        dataSet2.setColor(Color.rgb(0, 155, 0));
//        dataSet2.setCircleColor(Color.rgb(0,0,155));
//        dataSet2.setDrawFilled(true);
//        dataSet2.setDrawCircleHole(false);
//        dataSet2.setFillColor(Color.rgb(0, 80, 0));
//
//        // Combines the two separate datasets into one ArrayList to feed into the LineData object.
//        ArrayList dataSetFinal = new ArrayList<>();
//        dataSetFinal.add(dataSet1);
//        dataSetFinal.add(dataSet2);
//
//        // *Data is an object for even further customizing your specific graph output.
//        LineData lineData = new LineData(dataSetFinal);
//        lChart.getXAxis().setTextSize(200);
//        lChart.getXAxis().setGranularity(1); // Sets the 'step' of the x-axis sort of
//        lChart.getAxisLeft().setDrawLabels(false); // Disables the left y-axis
//
//        lChart.setData(lineData);
//        lChart.invalidate(); // refresh
//    }
}