package com.example.swastha;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.JsonIOException;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class Upload extends Fragment {
    EditText bloodPressure,bloodSugar,cholestrol,heartRate;
    Button predictButton;
    TextView predictionValue;
    Interpreter interpreter;
    private FirebaseFirestore fireStore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    public Upload() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_upload, container, false);
        fireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        try {
            interpreter = new Interpreter(loadModelFile(),null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        predictButton = root.findViewById(R.id.predictButton);
        bloodPressure = root.findViewById(R.id.bloodPressureIn);
        bloodSugar = root.findViewById(R.id.bloodSugarIn);
        cholestrol = root.findViewById(R.id.Cholestrol);
        heartRate = root.findViewById(R.id.heartRate);
        predictionValue = root.findViewById(R.id.predictionValue);

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cholestrol,Bp,bp,BMI,heartrate,glucose
                String bp,bs,bmi,ch,hr;
                float[] input = new float[6];

                ch = cholestrol.getText().toString();
                bp = bloodPressure.getText().toString();
                String[] newBp = bp.split("/");
                bmi = "25";
                hr = heartRate.getText().toString();
                bs = bloodSugar.getText().toString();

                Map<String, Object> user = new HashMap<>();
                String userId = currentUser.getUid();
                user.put("cholestrol",ch);
                user.put("bloodPressure",bp);
                user.put("bmi",bmi);
                user.put("heartRate",hr);
                user.put("bloodSugar",bs);
                DocumentReference documentReference = fireStore.collection("users").document(userId);
                documentReference.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity().getApplicationContext(), "User Data Updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                input[0] = Float.parseFloat(ch);
                input[1] = Float.parseFloat(newBp[0]);
                input[2] = Float.parseFloat(newBp[1]);
                input[3] = Float.parseFloat(bmi);
                input[4] = Float.parseFloat(hr);
                input[5] = Float.parseFloat(bs);

                float[][] output = new float[1][1];
                interpreter.run(input,output);
                float f = output[0][0];
                predictionValue.setText("Result:"+f);
            }
        });
        return root;
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor assetFileDescriptor = getActivity().getAssets().openFd("linear.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long length = assetFileDescriptor.getLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,length);
    }

}