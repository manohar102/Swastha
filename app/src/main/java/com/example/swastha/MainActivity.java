package com.example.swastha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    BottomNavigationView btm_view;
    Bundle arguments;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_HEART_RATE = "HEART_RATE";
    public static final String CONNECTION_STATE = "CONNECTION_STATE";

    //Service Varaible
    public static String DEVICE_NAME="device_name";
    public static String DEVICE_ADDRESS="device_address";
    public static String HEART_RATE="No Data";
    public static String CONNECTION="conneting...";

    //FireStore
    private FirebaseFirestore fireStore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Date date;

    //Graph
    public ArrayList<Integer> HeartRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        arguments = new Bundle();
        HeartRate = new ArrayList<>();

        btm_view = findViewById(R.id.bottom_view);
        btm_view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.home){
//                    getSupportActionBar().setTitle("Home");
                    arguments.putString(EXTRAS_DEVICE_NAME,DEVICE_NAME);
                    arguments.putString(EXTRAS_DEVICE_ADDRESS,DEVICE_ADDRESS);
                    if(HeartRate.size()==0){
                        HeartRate.add(0);
                    }
                    arguments.putIntegerArrayList("HeartSeries",HeartRate);
                    getLoadFragment(new Home(),arguments);
//                    Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                }
                else if (item.getItemId()==R.id.upload){
//                    getSupportActionBar().setTitle("Upload");
                    getLoadFragment(new Upload(),arguments);
//                    Toast.makeText(MainActivity.this, "Upload", Toast.LENGTH_SHORT).show();
                }
                else if (item.getItemId()==R.id.records){
//                    getSupportActionBar().setTitle("Upload");
                        Intent intent = new Intent(getApplicationContext(), Records.class);
                        startActivity(intent);
//                    Toast.makeText(MainActivity.this, "Upload", Toast.LENGTH_SHORT).show();
                }
                else if (item.getItemId()==R.id.profile){
//                    getSupportActionBar().setTitle("Profile");
                    getLoadFragment(new Profile(),arguments);
//                    Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser!=null) {
            fireStore.collection("users").whereEqualTo("userUid", currentUser.getUid())
                    .limit(1).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    processData(document.getData());
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }
    private void getHeartRateData(){
        date = new Date();
        String TODAY_DATE = DateFormat.format("yyyyMMdd", date.getTime()).toString();
        DocumentReference documentReference = fireStore.document("users/"+currentUser.getUid()+"/Workouts/"+TODAY_DATE);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        processHeartRateData(document.getData());
                    } else {
                        Log.d(TAG, "No Such Document");
                    }
                }else{
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void processHeartRateData(Map<String, Object> data){
        Log.d("data",data.toString());
        SortedSet<String> keys = new TreeSet<>(data.keySet());
        for (String key : keys) {
            ArrayList<String> val = (ArrayList<String>) data.get(key);
            Integer sum =0;
            if(!val.isEmpty()){
                for(String v: val){
                    sum += Integer.valueOf(v);
                }
            }
            HeartRate.add((int) (sum.doubleValue()/val.size()));
        }
        Log.d("DATA:",HeartRate.toString());
    }

    private void processData(Map<String, Object> data) {
        DEVICE_NAME = data.get("deviceName").toString();
        DEVICE_ADDRESS = data.get("deviceAddress").toString();
        CONNECTION = "Connecting...";
        getHeartRateData();

        stopService(new Intent(getBaseContext(),HeartRateService.class));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startService(new Intent(getBaseContext(),HeartRateService.class));
            }
        },2000);

//        arguments.putString(EXTRAS_DEVICE_NAME,DEVICE_NAME);
//        arguments.putString(EXTRAS_DEVICE_ADDRESS,DEVICE_ADDRESS);
//        arguments.putString(CONNECTION_STATE,CONNECTION);
        if(HeartRate.size()==0){
            HeartRate.add(0);
        }
        arguments.putIntegerArrayList("HeartSeries",HeartRate);
        getLoadFragment(new Home(),arguments);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(new Intent(getBaseContext(),HeartRateService.class));
    }

    private void getLoadFragment(Fragment fragment, Bundle arg) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        if(arg!=null){
            fragment.setArguments(arg);
        }
        fragmentTransaction.replace(R.id.container,fragment);
        fragmentTransaction.commit();
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (HeartRateService.ACTION_DATA_AVAILABLE.equals(action)) {
                String hR = intent.getStringExtra(HeartRateService.EXTRA_DATA);
                CONNECTION = "Connected";
                HEART_RATE = hR;
            }
            else if(HeartRateService.ACTION_GATT_CONNECTED.equals(action)){
                CONNECTION = "Connected";
                HEART_RATE = "No Data";
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HeartRateService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(HeartRateService.ACTION_GATT_CONNECTED);
        return intentFilter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}