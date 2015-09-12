package com.karthik.fbchatheadexample;

import java.util.List;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A placeholder fragment containing a simple view.
 */
public class FBChatHeadActivityFragment extends Fragment implements View.OnClickListener{

    private static Context mContext;
    private Intent serviceIntent;
    public FBChatHeadActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_layout, container, false);
        mContext = getActivity();
        setupClickListners(rootView);
        return rootView;
    }


    private void setupClickListners(View rootview){
        Button start = (Button) rootview.findViewById(R.id.start_floating);
        Button stop = (Button) rootview.findViewById(R.id.stop_floating);

        start.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_floating:
                displayButton();
                break;
            case R.id.stop_floating:
                //remove the chat head
                if(isServiceRunning("ChatHeadService")){
                    mContext.stopService(serviceIntent);
                }
                break;
        }
    }


    public static boolean isServiceRunning(String serviceClassName){
        final ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            Log.e("SERVICE",runningServiceInfo.process);
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }


    private void displayButton(){
        //starting the chat bubble service
        serviceIntent = new Intent(mContext, ChatHeadService.class);
        mContext.startService(serviceIntent);
    }

}
