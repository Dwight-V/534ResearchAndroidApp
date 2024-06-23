package com.example.a534researchandroidapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int GATT_MAX_MTU_SIZE = 517;
    private Button btnUpdateTxtOut, btnScan;
    private TextView txtOut;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private ScanSettings scanSettings;
    private ScanCallback scanCallback;
    private ScanResultAdapter scanResultAdapter;
    private RecyclerView scanResultsRecyclerView;

    private boolean isScanning = false;
    private List<ScanResult> scanResults = new ArrayList<>();

    private BluetoothGatt connectedGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUpdateTxtOut = (Button) findViewById(R.id.btnUpdateTxtOut);
        btnScan = (Button) findViewById(R.id.btnScan);
        txtOut = (TextView) findViewById(R.id.txtOut);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        scanCallback = new ScanCallback() {
            @Override
            @SuppressLint("MissingPermission")
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                int indexQuery = -1;
                for (int i = 0; i < scanResults.size(); i++) {
                    if (scanResults.get(i).getDevice().getAddress().equals(result.getDevice().getAddress())) {
                        indexQuery = i;
                        break;
                    }
                }
                if (indexQuery != -1) { // A scan result already exists with the same address
                    scanResults.set(indexQuery, result);
                    scanResultAdapter.notifyItemChanged(indexQuery);
                } else {
                    BluetoothDevice device = result.getDevice();
                    String name = device.getName() != null ? device.getName() : "Unnamed";
                    String address = device.getAddress();
                    Log.i("ScanCallback", "Found BLE device! Name: " + name + ", address: " + address);

                    scanResults.add(result);
                    scanResultAdapter.notifyItemInserted(scanResults.size() - 1);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e("ScanCallback", "onScanFailed: code " + errorCode);
            }
        };

        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                String deviceAddress = gatt.getDevice().getAddress();
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.w("BluetoothGattCallback", "Successfully connected to " + deviceAddress);
                        connectedGatt = gatt;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                connectedGatt.discoverServices();
                            }
                        });
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.w("BluetoothGattCallback", "Successfully disconnected from " + deviceAddress);
                        gatt.close();
                    }
                    // Assume the worst case; that we're working with the minimum packet ATT MTU size of 23. Anything larger is just a bonus.
                    // If we write to the bluetooth device over our packet size, we'll see a GATT_INVALID_ATTRIBUTE_LENGTH error.
                    gatt.requestMtu(GATT_MAX_MTU_SIZE);
                } else {
                    Log.w("BluetoothGattCallback", "Error " + status + " encountered for " + deviceAddress + "! Disconnecting...");
                    gatt.close();
                }
            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.w("BluetoothGattCallback", "Discovered " + gatt.getServices().size() + " services for " + gatt.getDevice().getAddress());
                    printGattTable(gatt); // Call to the printGattTable method implemented earlier
                    // Consider connection setup as complete here
                }
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                Log.w("BluetoothGattCallback", "ATT MTU changed to " + mtu + ", success: " + (status == BluetoothGatt.GATT_SUCCESS));
            }

        };


        scanResultAdapter = new ScanResultAdapter(scanResults, new ScanResultAdapter.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(ScanResult result) {
                if (isScanning) {
                    stopBleScan();
                }
                BluetoothDevice device = result.getDevice();
                Log.w("ScanResultAdapter", "Connecting to " + device.getAddress());
                device.connectGatt(MainActivity.this, false, gattCallback);
            }
        });



        setupRecyclerView();


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
                if (isScanning) {
                    stopBleScan();
                    btnScan.setText("Start Scan");
                } else {
                    startBleScan(MainActivity.this);
                    btnScan.setText("Stop Scan");
                }

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

    @SuppressLint({"MissingPermission", "NotifyDataSetChanged"})
    private void startBleScan(Context context) {
        if (!hasRequiredBluetoothPermissions(context)) {
            requestRelevantRuntimePermissions((Activity) context);
        } else {
            scanResults.clear();
            scanResultAdapter.notifyDataSetChanged();
            bleScanner.startScan(null, scanSettings, scanCallback);
            isScanning = true;
        }
    }

    @SuppressLint("MissingPermission")
    private void stopBleScan() {
        bleScanner.stopScan(scanCallback);
        isScanning = false;
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

    private void setupRecyclerView() {
        scanResultsRecyclerView = findViewById(R.id.scan_results_recycler_view);
        scanResultsRecyclerView.setAdapter(scanResultAdapter);
        scanResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        scanResultsRecyclerView.setNestedScrollingEnabled(false);

        RecyclerView.ItemAnimator animator = scanResultsRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    private void printGattTable(BluetoothGatt gatt) {
        List<BluetoothGattService> services = gatt.getServices();
        if (services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?");
            return;
        }
        for (BluetoothGattService service : services) {
            StringBuilder characteristicsTable = new StringBuilder("|--");
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                characteristicsTable.append(characteristic.getUuid().toString()).append("\n|--");
            }
            // Remove the last appended "|--"
            if (characteristicsTable.length() >= 3) {
                characteristicsTable.setLength(characteristicsTable.length() - 3);
            }
            Log.i("printGattTable", "\nService " + service.getUuid() + "\nCharacteristics:\n" + characteristicsTable.toString());
        }
    }

}