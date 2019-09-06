package com.example.tracksbasetest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.tracksbasetest.FragmentAdapter.FragmentHub;
import com.example.tracksbasetest.fcm.Data;
import com.example.tracksbasetest.fcm.FirebaseCloudMessage;
import com.example.tracksbasetest.utility.FCM;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.okhttp.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static androidx.core.content.ContextCompat.checkSelfPermission;


public class BroadcasterFragment extends Fragment implements View.OnClickListener {

    private static int RC_PERMISSION_LOCATION = 4505;
    private static String BASE_URL = "https://fcm.googleapis.com/fcm/";

    private FragmentHub myHub;
    private Activity parentActivity;

    private EditText txtBrdMsg;

    private Button btnBrdMsg;
    private Button btnClrMsg;

    private ProgressBar progressBar;

    private FirebaseFirestore firestore;

    private ArrayList<String> msgTokens = new ArrayList<>();

    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_broadcaster,
                container, false);

        txtBrdMsg = fragmentView.findViewById(R.id.txt_brdMsg);

        btnBrdMsg = fragmentView.findViewById(R.id.btn_brdMsg);
        btnClrMsg = fragmentView.findViewById(R.id.btn_clrMsg);

        progressBar = fragmentView.findViewById(R.id.progressBar);

        btnBrdMsg.setOnClickListener(this);
        btnClrMsg.setOnClickListener(this);

        restoreFragState();

        return fragmentView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        parentActivity = getActivity();

        if(context instanceof  FragmentHub){
            myHub = (FragmentHub) context;

            myHub.addFragment(this);
        }
        else{
            try {
                throw new Exception("FragmentActivity must implement FragmentHub " +
                        "Source: " + parentActivity);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        firestore = FirebaseFirestore.getInstance();

        retrieveMessageTokens();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void restoreFragState() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_brdMsg:
                uploadMessage();
                break;
            case R.id.btn_clrMsg:
                clearMessage();
        }
    }

    private void uploadMessage() {

        if (getContext() != null && parentActivity != null){
            if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(parentActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)){
                    showPermissionReason();
                }
                else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            RC_PERMISSION_LOCATION);
                }
                return;
            }
        }
        else{
            makeToast("Sorry, broadcast failed");
            return;
        }

        setLoadScreen(true);

        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, true));

        List<Double> epicentre = new ArrayList<>();

        epicentre.add(location.getLatitude());
        epicentre.add(location.getLongitude());

        debugLog("Location: " + epicentre.toString());


        String message = txtBrdMsg.getText().toString();
        BroadcastMessageData messageData = new BroadcastMessageData(epicentre, message);

        DocumentReference docRef = firestore.collection("users")
                .document(UserAuth.userId())
                .collection("messages").document();
        docRef.set(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                makeToast("Message Broadcast Successful");
                clearMessage();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast("Sorry, broadcast failed");
            }
        })
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setLoadScreen(false);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FCM fcmAPI = retrofit.create(FCM.class);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "key=" + UserAuth.retrieveServerKey());

        for (String token : msgTokens){
            Data data = new Data();
            data.setMessageData(messageData);
            data.setDataType(getString(R.string.dataType_brdcastMsg));

            FirebaseCloudMessage firebaseCloudMessage = new FirebaseCloudMessage();
            firebaseCloudMessage.setData(data);
            firebaseCloudMessage.setTo(token);

            Call<ResponseBody> call = fcmAPI.send(headers, firebaseCloudMessage);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    debugLog("Server Response: " + response.toString());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    makeToast("Failed to broadcast message via FCM");
                }
            });
        }
    }

    private void retrieveMessageTokens(){
        CollectionReference colRef = firestore.collection("users");

        colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document:
                     queryDocumentSnapshots.getDocuments()) {
                    msgTokens.add(document.getString("Messaging Token"));

                    debugLog("Added Message Token: " + msgTokens.get(msgTokens.size()-1));
                }
            }
        });
    }

    private void setLoadScreen(boolean set){
        progressBar.setVisibility(set ? View.VISIBLE : View.INVISIBLE);
        btnBrdMsg.setEnabled(!set);
        btnClrMsg.setEnabled(!set);
    }

    private void showPermissionReason(){
        MessageDialog reasonDialog = new MessageDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        RC_PERMISSION_LOCATION);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), R.string.perm_denied_msg, Toast.LENGTH_SHORT).show();
            }
        }, parentActivity.getString(R.string.loc_perm_reason), parentActivity);
        myHub.showDialogFragment(reasonDialog);
    }

    private void clearMessage(){
        txtBrdMsg.setText(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == RC_PERMISSION_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                uploadMessage();
            }
            else{
                Toast.makeText(getContext(), "You can't broadcast " +
                        "without granting this permission", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void debugLog(String message){
        Log.d("Broadcaster", message);
    }

    private void makeToast(String message){
        Log.d("Toast", message);
        Toast.makeText(parentActivity, message, Toast.LENGTH_SHORT).show();
    }
}
