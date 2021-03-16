package com.example.swastha;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HeartRateService extends Service {

    private static final String TAG = HeartRateService.class.getSimpleName();

    //FireStore
    private FirebaseFirestore fireStore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    ArrayList<String> HeartRate;
    Date date;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic heartRateCharacteristic=null;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";

    public static String HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_CONTROL_POINT = "00002a39-0000-1000-8000-00805f9b34fb";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(HEART_RATE_MEASUREMENT);

    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    public HeartRateService() {
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                startForeground("Connected");
                broadcastUpdate(ACTION_GATT_CONNECTED);
                if(heartRateCharacteristic==null){
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                }
                else{
                    readHeartData();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                startForeground("Disconnected");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                getSupportedGattServices();
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        HeartRate = new ArrayList<String>();
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        if(currentUser!=null) {
            fireStore.collection("users").whereEqualTo("userUid", currentUser.getUid())
                    .limit(1).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String DEVICE_ADDRESS = document.getData().get("deviceAddress").toString();
                                    registerDeviceAddress(DEVICE_ADDRESS);
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

    }

    private void registerDeviceAddress(String DEVICE_ADDRESS){
        startForeground(DEVICE_ADDRESS);
        if(DEVICE_ADDRESS!=null){
            startForeground("Establishing Connection..");
            processData(DEVICE_ADDRESS);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    private void processData(String DEVICE_ADDRESS) {
        if(intialize()){
            startForeground("Establishing Connection...");
            if(connect(DEVICE_ADDRESS)) {
                startForeground("Connecting...");
                if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                    startForeground("Service Diconnected! Open Swastha to Re-Establish Connection");
                    Log.w(TAG, "BluetoothAdapter not initialized");
                }
                else {
                    if(heartRateCharacteristic!=null){
                        Log.d("DEVICE:",DEVICE_ADDRESS+"_"+"Connected!");
                        startForeground(DEVICE_ADDRESS+"_"+"Connected!");
                        readHeartData();
                    }
                }
            }
        }
        else{
            startForeground("Enable Bluetooth! Open Swastha to Re-Establish Connection");
            disconnect();
            close();
        }
    }


    private void startForeground(String context) {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(context)
                .setContentIntent(pendingIntent)
                .build());
    }

    private void broadcastUpdate(String action) {
        final Intent intent = new Intent(action);
        if(ACTION_GATT_DISCONNECTED.equals(action)){
            if(!HeartRate.isEmpty()) {
                date = new Date();
                String TODAY_DATE = DateFormat.format("yyyyMMdd", date.getTime()).toString();
                String TIME = DateFormat.format("HHmmssSS", date.getTime()).toString();

                DocumentReference documentReference = fireStore.collection("users").document(currentUser.getUid())
                        .collection("Workouts").document(TODAY_DATE);

                Map<String, Object> hR = new HashMap<>();
                hR.put(TIME,HeartRate);
                documentReference.set(hR, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HeartRate.clear();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HeartRateService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
            HeartRate.add(String.valueOf(heartRate));
            startForeground(String.valueOf(heartRate));
        }
        else{
            Log.d(TAG,"UUID Not matching"+characteristic.getUuid().toString());
        }
        sendBroadcast(intent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        close();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
    }

    public boolean intialize(){
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        startForeground("Connecting..");
        return true;
    }
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void getSupportedGattServices() {
        List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if(UUID.fromString(HEART_RATE_MEASUREMENT).equals(gattCharacteristic.getUuid())){
                    heartRateCharacteristic = gattCharacteristic;
                }
            }
        }
        readHeartData();
    }

    public void readHeartData(){
        Log.d("readHeartData:","called");
        final int charaProp = heartRateCharacteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                setCharacteristicNotification( mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothGatt.readCharacteristic(heartRateCharacteristic);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = heartRateCharacteristic;
            setCharacteristicNotification(heartRateCharacteristic, true);
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if(enabled){
            startForeground("Waiting for Data...");
        }
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
}