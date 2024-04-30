package com.example.a534researchandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private Button btnUpdateTxtOut;
    private TextView txtOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUpdateTxtOut = (Button) findViewById(R.id.btnUpdateTxtOut);
        txtOut = (TextView) findViewById(R.id.txtOut);

        btnUpdateTxtOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtOut.setText(btnUpdateTxtOut.getText().toString());
            }
        });
    }
}