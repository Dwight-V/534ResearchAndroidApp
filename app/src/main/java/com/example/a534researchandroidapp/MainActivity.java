package com.example.a534researchandroidapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Button btnUpdateTxtOut, btnScan;
    private TextView txtOut;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUpdateTxtOut = (Button) findViewById(R.id.btnUpdateTxtOut);
        btnScan = (Button) findViewById(R.id.btnScan);
        txtOut = (TextView) findViewById(R.id.txtOut);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        btnUpdateTxtOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtOut.setText(btnUpdateTxtOut.getText().toString());
            }
        });

//        Toast.makeText(this, "User has permissions: " + hasRequiredBluetoothPermissions(this), Toast.LENGTH_SHORT).show();
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBleScan(MainActivity.this);
            }
        });

    }

    // ************************************************************************ All from https://punchthrough.com/android-ble-guide/
    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            promptEnableBluetooth();
        }
    }

    private final ActivityResultLauncher<Intent> bluetoothEnablingResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Bluetooth is enabled, good to go
                } else {
                    // User dismissed or denied Bluetooth prompt
                    promptEnableBluetooth();
                }
            }
    );

    /**
     * Prompts the user to enable Bluetooth via a system dialog.
     *
     * For Android 12+, {@link android.Manifest.permission#BLUETOOTH_CONNECT} is required to use
     * the {@link BluetoothAdapter#ACTION_REQUEST_ENABLE} intent.
     */
    private void promptEnableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Insufficient permission to prompt for Bluetooth enabling
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnablingResult.launch(enableBtIntent);
        }
    }

    /**
     * Determine whether the current {@link Context} has been granted the relevant {@link Manifest.permission}.
     */
    public static boolean hasPermission(Context context, String permissionType) {
        return ContextCompat.checkSelfPermission(context, permissionType) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Determine whether the current {@link Context} has been granted the relevant permissions to perform
     * Bluetooth operations depending on the mobile device's Android version.
     */
    public static boolean hasRequiredBluetoothPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return hasPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            return hasPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void startBleScan(Context context) {
        if (!hasRequiredBluetoothPermissions(context)) {
            requestRelevantRuntimePermissions((Activity) context);
        } else {
            // TODO: Actually perform scan
        }
    }

    private void requestRelevantRuntimePermissions(Activity activity) {
        if (hasRequiredBluetoothPermissions(activity)) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            requestLocationPermission(activity);
        } else {
            requestBluetoothPermissions(activity);
        }
    }

    private void requestLocationPermission(Activity activity) {
        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                .setTitle("Location permission required")
                .setMessage(
                        "Starting from Android M (6.0), the system requires apps to be granted " +
                                "location access in order to scan for BLE devices."
                )
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(
                        activity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE
                ))
                .show());
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private void requestBluetoothPermissions(Activity activity) {
        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                .setTitle("Bluetooth permission required")
                .setMessage(
                        "Starting from Android 12, the system requires apps to be granted " +
                                "Bluetooth access in order to scan for and connect to BLE devices."
                )
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(
                        activity,
                        new String[]{
                                android.Manifest.permission.BLUETOOTH_SCAN,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                        },
                        PERMISSION_REQUEST_CODE
                ))
                .show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_REQUEST_CODE) return;

        boolean containsPermanentDenial = false;
        boolean containsDenial = false;
        boolean allGranted = true;

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                containsDenial = true;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    containsPermanentDenial = true;
                }
            } else {
                allGranted = false;
            }
        }

        if (containsPermanentDenial) {
            // TODO: Handle permanent denial (e.g., show AlertDialog with justification)
            // Note: The user will need to navigate to App Settings and manually grant
            // permissions that were permanently denied
        } else if (containsDenial) {
            requestRelevantRuntimePermissions(this);
        } else if (allGranted && hasRequiredBluetoothPermissions(this)) {
            startBleScan(this);
        } else {
            // Unexpected scenario encountered when handling permissions
            recreate();
        }
    }
}