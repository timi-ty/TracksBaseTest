package com.example.tracksbasetest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseUser;

import static com.example.tracksbasetest.FragmentAdapter.ACTIVE_FRAGMENT;
import static com.example.tracksbasetest.FragmentAdapter.BROADCASTER_FRAGMENT;
import static com.example.tracksbasetest.FragmentAdapter.FRC_SIGN_IN;
import static com.example.tracksbasetest.FragmentAdapter.FRC_SIGN_OUT;
import static com.example.tracksbasetest.FragmentAdapter.FRC_SWITCH_FRAGMENT;
import static com.example.tracksbasetest.FragmentAdapter.FragmentHub;
import static com.example.tracksbasetest.FragmentAdapter.RECEIVER_FRAGMENT;
import static com.example.tracksbasetest.UserAuth.attachAuthListener;
import static com.example.tracksbasetest.UserAuth.detachAuthListener;
import static com.example.tracksbasetest.UserAuth.signIn;
import static com.example.tracksbasetest.UserAuth.signOut;

public class CentralActivity extends AppCompatActivity implements FragmentHub{

    private static String CHANNEL_ID = "Channel_1";

    FragmentManager fragmentManager;

    ReceiverFragment receiverFragment;
    BroadcasterFragment broadcasterFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central);

        createNotificationChannel();

        UserAuth.init(this);

        if(fragmentManager == null){
            fragmentManager = getSupportFragmentManager();
        }

        if(savedInstanceState == null){
            switchToReceiverFragment();
        }
    }

    private void switchToBroadcasterFragment() {
        broadcasterFragment = new BroadcasterFragment();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, broadcasterFragment)
                .addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void switchToReceiverFragment() {
        receiverFragment = new ReceiverFragment();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, receiverFragment)
                .addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onPause() {
        detachAuthListener();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        attachAuthListener();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        switch (requestCode){
            case FRC_SWITCH_FRAGMENT:
                String state = intent.getStringExtra(ACTIVE_FRAGMENT);
                switch (state){
                    case RECEIVER_FRAGMENT:
                        switchToReceiverFragment();
                        break;
                    case BROADCASTER_FRAGMENT:
                        switchToBroadcasterFragment();
                }
                break;
            case FRC_SIGN_IN:
                signIn(this);
                break;
            case FRC_SIGN_OUT:
                signOut();
                break;
            default:
                super.startActivityForResult(intent, requestCode, options);
                break;
        }
    }

    @Override
    public void onAuthChanged(@Nullable FirebaseUser user) {
        for (Fragment fragment:
                attachedFragments) {
            if(fragment instanceof FragmentAdapter.LinkedFragment){
                ((FragmentAdapter.LinkedFragment) fragment).onAuthChanged(user);
            }
            else{
                try {
                    throw new Exception("Fragment must implement LinkedFragment " +
                            "Source: " + fragment);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void showDialogFragment(DialogFragment dialogFragment) {
        dialogFragment.show(fragmentManager, "Dialog");
    }

    @Override
    public void addFragment(Fragment fragment) {
        attachedFragments.add(fragment);
    }

    @Override
    public void removeFragment(Fragment fragment) {
        attachedFragments.remove(fragment);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
