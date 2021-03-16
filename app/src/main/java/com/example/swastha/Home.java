package com.example.swastha;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.internal.ConnectionErrorMessages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static android.content.Context.BIND_AUTO_CREATE;

public class Home extends Fragment {
    String TAG = Home.class.getSimpleName();
    String device_address="Unknown", connection_state="";
    Double heart_data=0.0;
    Bundle arg;
    TextView deviceAddress,dataValue,connectionState;
    Button mStartHeart,mStopHeart;

    ArrayList<Integer> heartRateData;
    ArrayList<Integer> heartRate;
    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    Context _context;
    public static String HEART_RATE="0";
    public static String CONNECTION="conneting...";

    public Home() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        _context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        heartRateData= new ArrayList<>();
        heartRate= new ArrayList<>();
        series = new LineGraphSeries<DataPoint>();
    }

    @Override
    public void onResume() {
        super.onResume();
        _context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        _context.unregisterReceiver(mGattUpdateReceiver);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (HeartRateService.ACTION_DATA_AVAILABLE.equals(action)) {
                String hR = intent.getStringExtra(HeartRateService.EXTRA_DATA);
                HEART_RATE = hR;
                heartRate.add(Integer.valueOf(HEART_RATE));
                dataValue.setText(HEART_RATE);
            }
            else if(HeartRateService.ACTION_GATT_CONNECTED.equals(action)){
                CONNECTION = "Connected";
                dataValue.setText(HEART_RATE);
            }
            else if(HeartRateService.ACTION_GATT_DISCONNECTED.equals(action)){
                CONNECTION = "Disconnected";
                Integer sum =0;
                for(Integer val:heartRate){
                    sum+=val;
                }
                addEntry(sum.doubleValue()/heartRate.size());
                heartRate.clear();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);
        dataValue = root.findViewById(R.id.heartRateValue);

        GraphView graph = root.findViewById(R.id.graph);
        graph.addSeries(series);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(150);
        arg = getArguments();
        if(arg!=null && series.isEmpty()){
            heartRateData.clear();
            HEART_RATE = arg.getString(MainActivity.EXTRAS_HEART_RATE);
            ArrayList<Integer> arList  = arg.getIntegerArrayList("HeartSeries");
            if(arList!=null){
                heartRateData.addAll(arList);
                processData();
            }
            Log.d("FRAG_DATA:",heartRateData.toString());
        }
        dataValue.setText(HEART_RATE);


//        arg = getArguments();
//        if(arg!=null){
////            device_address = arg.getString(MainActivity.EXTRAS_DEVICE_ADDRESS);
////            String temp = arg.getString(MainActivity.HEART_RATE);
////            if(temp!=null){
////                heart_data = Double.valueOf(temp);
////            }
//////            connection_state = arg.getString(MainActivity.CONNECTION_STATE);
////            dataValue.setText(arg.getString(MainActivity.EXTRAS_HEART_RATE));
//
//            heartRate.addAll(arg.getIntegerArrayList("HeartSeries"));
//
//        }
//        else{
//            heartRate.add(0);
//        }
//        deviceAddress.setText(device_address);
//        dataValue.setText(String.valueOf(heart_data));
//        connectionState.setText(connection_state);

        return root;
    }

    private void processData() {
        if(series.isEmpty()) {
            for (int i = 0; i < heartRateData.size(); i++) {
                Double value = Double.valueOf(heartRateData.get(i));
                addEntry(value);
            }
        }
    }

    private void addEntry(Double value) {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        series.appendData(new DataPoint(lastX++, value), true, 10);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HeartRateService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(HeartRateService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(HeartRateService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }


}