package com.example.tracksbasetest;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAuth {

    private static final int RC_SIGN_IN = 9001;

    private static FirebaseAuth firebaseAuth;
    private static FirebaseAuth.AuthStateListener authStateListener;

    private static String server_key;

    static void init(final Context context) {
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    Toast toast = Toast.makeText(context, "Signed out", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                            0, 100);
                    toast.show();
                }
                else{
                    Toast toast = Toast.makeText(context, "Signed in as " +
                            firebaseAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                            0, 100);
                    toast.show();
                }
                if(context instanceof FragmentAdapter.FragmentHub){
                    ((FragmentAdapter.FragmentHub) context)
                            .onAuthChanged(firebaseAuth.getCurrentUser());
                }
                else{
                    try {
                        throw new Exception("FragmentActivity must implement FragmentHub " +
                                "Source: " + context);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        regToFCM(instanceIdResult.getToken());
                    }
                });
            }
        };
    }

    static void attachAuthListener(){
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    static void detachAuthListener(){
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    static void signIn(Activity caller){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());


        caller.startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).build(), RC_SIGN_IN);
    }

    static void signOut(){
        firebaseAuth.signOut();
    }

    static String userId(){
        return firebaseAuth.getUid();
    }

    public static void regToFCM(String userToken){
        Map<String, String> map = new HashMap<>();
        map.put("Messaging Token", userToken);

        FirebaseFirestore.getInstance().collection("users")
        .document(UserAuth.userId())
        .set(map);

        retrieveServerKey();
    }

    public static String retrieveServerKey(){
        if(server_key == null){
            DocumentReference servRef = FirebaseFirestore.getInstance()
                    .collection("app-generics")
                    .document("server");

            servRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    server_key = documentSnapshot.getString("server_key");

                    debugLog("Server Key: " + server_key);
                }
            });

            return null;
        }
        else
            return server_key;
    }

    private static void debugLog(String message){
        Log.d("Firebase Auth", message);
    }
}
