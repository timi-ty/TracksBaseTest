package com.example.tracksbasetest;


import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

class FragmentAdapter {

    static final int FRC_SWITCH_FRAGMENT = 1021;
    static final int FRC_SIGN_IN = 9007;
    static final int FRC_SIGN_OUT = 9011;

    static final String ACTIVE_FRAGMENT = "Launch";

    static final String RECEIVER_FRAGMENT = "re";

    static final String BROADCASTER_FRAGMENT = "br";

    public interface LinkedFragment {
        void onAuthChanged(@Nullable FirebaseUser user);
    }

    public interface FragmentHub{
        ArrayList<Fragment> attachedFragments = new ArrayList<>();
        void onAuthChanged(@Nullable FirebaseUser user);
        void showDialogFragment(DialogFragment dialogFragment);
        void addFragment(Fragment fragment);
        void removeFragment(Fragment fragment);
    }
}
