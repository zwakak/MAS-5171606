package com.example.jade_test2;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;

import jade.android.RuntimeCallback;
import jade.core.AgentContainer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
public class AppController extends Application {
    private static AppController sInstance;
    private static Activity activeActivity;


    public static synchronized AppController getInstance() {

        return sInstance;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        setupActivityListener();


    }

    /*@Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }*/

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void setupActivityListener() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                activeActivity =activity;
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                activeActivity =activity;
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public static Activity getActiveActivity() {
        return activeActivity;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

}
