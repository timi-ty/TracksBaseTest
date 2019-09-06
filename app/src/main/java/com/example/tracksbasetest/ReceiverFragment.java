package com.example.tracksbasetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import static com.example.tracksbasetest.FragmentAdapter.*;

public class ReceiverFragment extends Fragment implements View.OnClickListener, LinkedFragment {

    private String SIGNED_IN = "signed_in";
    private String USER_NAME = "user_name";

    private Activity parentActivity;

    private Button btnSignIn;
    private Button btnSignOut;
    private Button btnBroadcast;
    private TextView tvUserName;
    private FragmentHub myHub;

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

    }

    @Override
    public void onDetach() {


        myHub.removeFragment(this);

        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_receiver,
                container, false);


        btnSignIn = fragmentView.findViewById(R.id.btn_sign_in);
        btnSignOut = fragmentView.findViewById(R.id.btn_sign_out);
        btnBroadcast = fragmentView.findViewById(R.id.btn_brdcast);

        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnBroadcast.setOnClickListener(this);

        tvUserName = fragmentView.findViewById(R.id.tv_user_name);

        restoreFragState();

        return fragmentView;
    }

    private void signIn(){
        parentActivity.startActivityForResult(new Intent(), FRC_SIGN_IN);
    }

    private void signOut(){
        parentActivity.startActivityForResult(new Intent(), FRC_SIGN_OUT);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_sign_in:
                signIn();
                break;
            case R.id.btn_sign_out:
                signOut();
                break;
            case R.id.btn_brdcast:
                Intent intent = new Intent().putExtra(ACTIVE_FRAGMENT, BROADCASTER_FRAGMENT);
                parentActivity.startActivityForResult(intent, FRC_SWITCH_FRAGMENT);
        }
    }

    @Override
    public void onAuthChanged(@Nullable FirebaseUser user) {
        Log.d("Auth", "Handling Auth Change");
        if(user != null){
            String greetings = String.format(getString(R.string.user_greetings),
                    user.getDisplayName());

            tvUserName.setText(greetings);
            btnSignIn.setEnabled(false);
            btnSignOut.setEnabled(true);
            btnBroadcast.setEnabled(true);
        }
        else {
            tvUserName.setText(R.string.anon_greetings);

            btnSignIn.setEnabled(true);
            btnSignOut.setEnabled(false);
            btnBroadcast.setEnabled(false);
        }

        saveFragState();
    }

    private void saveFragState() {
        Bundle args = new Bundle();
        args.putBoolean(SIGNED_IN, btnSignOut.isEnabled());
        args.putString(USER_NAME, (String) tvUserName.getText());
        setArguments(args);

        Log.d("Fragment", "Saved Fragment State");
    }

    private void restoreFragState(){
        Bundle args = getArguments();
        if(args != null){
            tvUserName.setText(args.getString(USER_NAME));

            btnSignIn.setEnabled(!args.getBoolean(SIGNED_IN));
            btnSignOut.setEnabled(args.getBoolean(SIGNED_IN));
            btnBroadcast.setEnabled(args.getBoolean(SIGNED_IN));

            Log.d("Fragment", "Restored Fragment State");
        }
    }
}
