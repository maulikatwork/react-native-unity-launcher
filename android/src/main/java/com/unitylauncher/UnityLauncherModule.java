package com.unitylauncher;

import android.content.Intent;
import android.util.Log;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActivityManager;
import android.content.Context;

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
    private Application.ActivityLifecycleCallbacks unityActivityCallbacks;
    private boolean isUnityActivityActive = false;

    // Unity activity states
    private enum UnityState {
        IDLE,        // Not running
        LAUNCHING,   // In the process of launching
        RUNNING,     // Running and active
        PAUSED,      // Temporarily paused
        STOPPING     // In the process of stopping
    }

    private UnityState unityState = UnityState.IDLE;
    private long lastLaunchTime = 0;

    public UnityLauncherModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
        context.addLifecycleEventListener(this);
        
        unityActivityCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (activity.getClass().getName().contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity created");
                    isUnityActivityActive = true;
                }
            }
            
            @Override
            public void onActivityStarted(Activity activity) {
                if (activity.getClass().getName().contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity started");
                }
            }
            
            @Override
            public void onActivityResumed(Activity activity) {
                if (activity.getClass().getName().contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity resumed");
                }
            }
            
            @Override
            public void onActivityPaused(Activity activity) {
                if (activity.getClass().getName().contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity paused");
                }
            }
            
            @Override
            public void onActivityStopped(Activity activity) {
                if (activity.getClass().getName().contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity stopped");
                }
            }
            
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                if (activity.getClass().getName().contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity state saved");
                }
            }
            
            @Override
            public void onActivityDestroyed(Activity activity) {
                if (activity.getClass().getName().contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity destroyed");
                    isUnityActivityActive = false;
                    isUnityRunning = false;
                    
                    if (unityReturnCallback != null) {
                        unityReturnCallback.invoke();
                        unityReturnCallback = null;
                    }
                    
                    System.gc();
                }
            }
        };
        
        ((Application) reactContext.getApplicationContext()).registerActivityLifecycleCallbacks(unityActivityCallbacks);
    }

    @Override
    public String getName() {
        return "UnityLauncher";
    }

    @ReactMethod
    public void launchUnity() {
        if (unityState == UnityState.IDLE || 
            (unityState == UnityState.STOPPING && System.currentTimeMillis() - lastLaunchTime > 500)) {
            unityState = UnityState.LAUNCHING;
            lastLaunchTime = System.currentTimeMillis();
            actuallyLaunchUnity();
        } else if (unityState == UnityState.PAUSED) {
            // Resume if paused
            bringUnityToForeground();
        } else {
            Log.w(TAG, "Unity launch requested while in state: " + unityState);
        }
    }
    
    @ReactMethod
    public void launchUnityWithCallback(Callback callback) {
        try {
            unityReturnCallback = callback;
            if (unityState == UnityState.IDLE || 
                (unityState == UnityState.STOPPING && System.currentTimeMillis() - lastLaunchTime > 500)) {
                unityState = UnityState.LAUNCHING;
                lastLaunchTime = System.currentTimeMillis();
                actuallyLaunchUnity();
            } else if (unityState == UnityState.PAUSED) {
                // Resume if paused
                bringUnityToForeground();
            } else {
                Log.w(TAG, "Unity launch requested while in state: " + unityState);
                if (callback != null) {
                    callback.invoke("Error: Unity already in state " + unityState);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error launching Unity with callback: " + e.getMessage());
            if (callback != null) {
                callback.invoke("Error: " + e.getMessage());
            }
        }
    }
    
    public static void onUnityReturn() {
        if (unityReturnCallback != null) {
            unityReturnCallback.invoke();
            unityReturnCallback = null;
        }
    }
    
    @Override
    public void onHostResume() {
        if (isUnityRunning) {
            isUnityRunning = false;
            
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
        unityReturnCallback = null;
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        ((Application) reactContext.getApplicationContext()).unregisterActivityLifecycleCallbacks(unityActivityCallbacks);
    }

    private void actuallyLaunchUnity() {
        Intent intent = new Intent(reactContext, com.unity3d.player.CustomUnityPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
        reactContext.startActivity(intent);
        isUnityRunning = true;
    }

    private boolean isUnityProcessRunning() {
        ActivityManager manager = (ActivityManager) reactContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.processName.equals(reactContext.getPackageName() + ":unity")) {
                return true;
            }
        }
        return false;
    }

    private void killUnityProcess() {
        ActivityManager manager = (ActivityManager) reactContext.getSystemService(Context.ACTIVITY_SERVICE);
        manager.killBackgroundProcesses(reactContext.getPackageName() + ":unity");
    }

    private void bringUnityToForeground() {
        // Implementation of bringing Unity to foreground
    }

    public static void prepareForUnityReturn() {
        Log.d(TAG, "Unity is preparing to return to React Native");
        // You can use this to prepare for Unity return if needed
    }

    public static void onUnityCleanupStarted() {
        Log.d(TAG, "Unity cleanup has started");
        // Update states as needed
    }

    public static void onUnityDestroyed() {
        Log.d(TAG, "Unity has been destroyed");
        // Call onUnityReturn if it wasn't called earlier
        onUnityReturn();
    }
}
