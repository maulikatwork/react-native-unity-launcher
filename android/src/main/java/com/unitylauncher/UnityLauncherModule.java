package com.unitylauncher;

import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;

public class UnityLauncherModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String TAG = "UnityLauncherModule";
    private final ReactApplicationContext reactContext;
    private static Callback unityReturnCallback;
    private boolean isUnityRunning = false;

    public UnityLauncherModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
        context.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "UnityLauncher";
    }

    @ReactMethod
    public void launchUnity() {
        try {
            Intent intent = new Intent(reactContext, com.unity3d.player.CustomUnityPlayerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            reactContext.startActivity(intent);
            isUnityRunning = true;
        } catch (Exception e) {
            Log.e(TAG, "Error launching Unity: " + e.getMessage());
        }
    }
    
    @ReactMethod
    public void launchUnityWithCallback(Callback callback) {
        try {
            unityReturnCallback = callback;
            Intent intent = new Intent(reactContext, com.unity3d.player.CustomUnityPlayerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            reactContext.startActivity(intent);
            isUnityRunning = true;
        } catch (Exception e) {
            Log.e(TAG, "Error launching Unity with callback: " + e.getMessage());
            if (callback != null) {
                callback.invoke("Error: " + e.getMessage());
            }
        }
    }
    
    // Method to be called when Unity returns
    public static void onUnityReturn() {
        if (unityReturnCallback != null) {
            unityReturnCallback.invoke();
            unityReturnCallback = null;
        }
    }
    
    @Override
    public void onHostResume() {
        // React Native activity is resuming
        if (isUnityRunning) {
            isUnityRunning = false;
            
            // Trigger callback if Unity was running before
            if (unityReturnCallback != null) {
                unityReturnCallback.invoke();
                unityReturnCallback = null;
            }
        }
    }

    @Override
    public void onHostPause() {
        // React Native activity is pausing
    }

    @Override
    public void onHostDestroy() {
        // React Native activity is being destroyed
        unityReturnCallback = null;
    }
}
