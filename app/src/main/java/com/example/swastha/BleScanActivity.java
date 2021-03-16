package com.example.swastha;


import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;


public class BleScanActivity extends AppCompatActivity {
//    private LeDeviceListAdapter mLeDeviceListAdapter;
    private final static String TAG = BleScanActivity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;
    private Handler mHandler;

    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> deviceScanList;
    private ArrayList<BluetoothDevice> deviceBleList;

    Dialog loader;

    private static final int REQUEST_ENABLE_BT = 11;
    private static final int REQUEST_ENABLE_LOCATION = 1;
    private ListView devicesList;
    private Button scanningBtn;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private final String DEVICE_NAME = "NAME";
    private final String DEVICE_ADDRESS = "ADDRESS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);

        loader = new Dialog(this);
        mHandler = new Handler();
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Start of BleScan
        devicesList = findViewById(R.id._dynamic);
//        scanningBtn = findViewById(R.id.ble_scan);
        swipeRefreshLayout = findViewById(R.id.swipeRefeshBle);

        deviceScanList = new ArrayList<String>();
        deviceBleList = new ArrayList<BluetoothDevice>();

        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,deviceScanList);
        devicesList.setAdapter(listAdapter);

//        scanningBtn.setOnClickListener(v -> {
//            if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled()){
//                clearUI();
//                scanLeDevice(true);
//            }
//        });

        devicesList.setOnItemClickListener((parent, view, position, id) -> {
            Object device = devicesList.getItemAtPosition(position);
            BluetoothDevice deviceAddress = deviceBleList.get(position);
            String[] cDevice = device.toString().split("\n");
            Intent intent = new Intent();
            intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, cDevice[0].trim());
            intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, cDevice[1].trim());
            setResult(101,intent);
            finish();
        });

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                        if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled()){
                            clearUI();
                            scanLeDevice(true);
                        }
                    }
                }
        );
    }

    private void clearUI(){
        deviceScanList.clear();
        deviceBleList.clear();
        listAdapter.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearUI();
        scanLeDevice(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearUI();
        scanLeDevice(true);
    }

    private void closePopup() {
        loader.dismiss();
    }

    private void showPopup() {
        loader.setContentView(R.layout.loader);
        loader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loader.show();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                }
//            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            swipeRefreshLayout.setRefreshing(false);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!deviceBleList.contains(device)){
                            deviceBleList.add(device);
                            deviceScanList.add(device.getName()+"\n"+device.getAddress());
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        };
}