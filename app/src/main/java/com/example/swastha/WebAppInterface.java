package com.example.swastha;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WebAppInterface {
    Context mContext;
    String data;
    String FileName;
    //FireStore
    private FirebaseFirestore fireStore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    WebAppInterface(Context ctx){
        mContext=ctx;
    }


    @JavascriptInterface
    public void sendData(String data) {
        //Get the string value to process
        fireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        this.data=data;
        Log.d("JS_data", "sendData: "+data);
        Date date = new Date();
        String TODAY_DATE = DateFormat.format("yyyyMMddHHmmssSS", date.getTime()).toString();
        
        DocumentReference documentReference = fireStore.collection("users").document(currentUser.getUid())
                .collection("Files").document("heartRecords");

        Map<String, String> hR = new HashMap<>();
        if(FileName==null) FileName="UnKnown";
        Log.d("FIlenaem:",FileName);
        hR.put(FileName,data);
        documentReference.set(hR, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(mContext, "Completed", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @JavascriptInterface
    public void sendName(String name){
        FileName = name;
    }
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();

    }
}
