package com.example.swastha;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import static android.content.Context.BIND_AUTO_CREATE;

public class Home extends Fragment {

    String device_address="Unknown", connection_state="";
    Double heart_data=0.0;
    Bundle arg;
    TextView deviceAddress,dataValue,connectionState;
    Button mStartHeart,mStopHeart;
    ArrayList<Integer> heartRate;
    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);
//        deviceAddress = root.findViewById(R.id.device_address);
//        dataValue = root.findViewById(R.id.data_value);
//        connectionState = root.findViewById(R.id.connection_state);
        heartRate= new ArrayList<>();
        arg = getArguments();

        if(arg!=null){
//            device_address = arg.getString(MainActivity.EXTRAS_DEVICE_ADDRESS);
//            String temp = arg.getString(MainActivity.HEART_RATE);
//            if(temp!=null){
//                heart_data = Double.valueOf(temp);
//            }
//            connection_state = arg.getString(MainActivity.CONNECTION_STATE);
            heartRate.addAll(arg.getIntegerArrayList("HeartSeries"));
        }
        else{
            heartRate.add(0);
        }

        GraphView graph = root.findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(150);
        processData();
//        deviceAddress.setText(device_address);
//        dataValue.setText(String.valueOf(heart_data));
//        connectionState.setText(connection_state);

        return root;
    }

    private void processData() {
        if(series.isEmpty()) {
            for (int i = 0; i < heartRate.size(); i++) {
                Double value = Double.valueOf(heartRate.get(i));
                addEntry(value);
            }
        }
    }

    private void addEntry(Double value) {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        series.appendData(new DataPoint(lastX++, value), true, 10);
    }


}