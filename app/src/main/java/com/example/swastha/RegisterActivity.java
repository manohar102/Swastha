package com.example.swastha;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = RegisterActivity.class.getSimpleName();

    //SignIn and FireBase Initializations
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;

    //UI Components
    private Button signInButton;
    ProgressBar SignInprogressBar;

    //Bluetooth and Location Initializations
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 11;
    private static final int REQUEST_ENABLE_LOCATION = 1;
    private static final int RC_SELECT_DEVICE = 101;

    //Foreground
    private static final int REQUEST_ENABLE_FOREGROUND = 200;

    //Custom Constants
    private final static int RC_SIGN_IN=9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

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

        signInButton=findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        SignInprogressBar = findViewById(R.id.sign_in_progressBar);
        SignInprogressBar.setVisibility(View.GONE);

        //requesting google to show signin options(gmail accounts)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    protected void onStart() {
        super.onStart();
        boolean locationPermission = checkSetLocationPermission();
        checkBluetoothState();
        currentUser = mAuth.getCurrentUser();
//        if(currentUser!=null){
//            SignInprogressBar.setVisibility(View.VISIBLE);
//            checkUserDevice(currentUser);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currentUser!=null){
            SignInprogressBar.setVisibility(View.VISIBLE);
            checkUserDevice(currentUser);
        }
    }

    private void signIn() {
        SignInprogressBar.setVisibility(View.VISIBLE);
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                SignInprogressBar.setVisibility(View.GONE);
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        //Result returned from launching the Intent from Bluetooth Enable
        else if (requestCode==REQUEST_ENABLE_BT){
            checkBluetoothState();
        }
        //Result returned from launching the Intent from Bluetooth Device Scan
        else if(requestCode==RC_SELECT_DEVICE){
            Bundle in = data.getExtras();
            String deviceName = in.getString(MainActivity.EXTRAS_DEVICE_NAME);
            String deviceAddress = in.getString(MainActivity.EXTRAS_DEVICE_ADDRESS);
            registerUserDevice(deviceName,deviceAddress);
        }
        else{
            Toast.makeText(this, "Id Not mached", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            currentUser = mAuth.getCurrentUser();
                            checkUserDevice(currentUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Sorry Authentication Failed", Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        }
    }

    public void updateUI(FirebaseUser user,boolean LoginSuccess){
        if(user!=null){
            if(LoginSuccess) {
                SignInprogressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Login Successfull", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            else{
                if(checkSetLocationPermission()){
                    checkBluetoothState();
                    startActivityForResult(new Intent(this,BleScanActivity.class),RC_SELECT_DEVICE);
                }
                else{
                    finish();
                }
            }
        }
    }

    private void checkUserDevice(FirebaseUser user) {
        if(user!=null) {
            fireStore.collection("users").whereEqualTo("userUid", user.getUid())
                    .limit(1).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean hasDevice = !(task.getResult().isEmpty());
                            updateUI(user, hasDevice);
                        } else {
                            Log.d("checkUserDevice:", "error");
                        }
                    });
        }
    }

    private void registerUserDevice(String deviceName, String deviceAddress) {
        Map<String, Object> user = new HashMap<>();
        String userId = currentUser.getUid();
        user.put("deviceName",deviceName);
        user.put("deviceAddress",deviceAddress);
        user.put("userUid",userId);
        DocumentReference documentReference = fireStore.collection("users").document(userId);
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegisterActivity.this, "Device Registered", Toast.LENGTH_SHORT).show();
                updateUI(currentUser,true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Permissions
    private boolean checkSetLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] { Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ENABLE_LOCATION);
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_ENABLE_LOCATION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Access Location allowed. You can scan devices", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Access Location forbidden. You can't scan devices", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void checkBluetoothState(){
        if(mBluetoothAdapter==null){
            Toast.makeText(this, "Bluetooth Not Supported on your device", Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBluetoothAdapter.isEnabled()){
                if(mBluetoothAdapter.isDiscovering()){
                    Toast.makeText(this, "Device discovering process...", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "You need to enable Bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }
}
