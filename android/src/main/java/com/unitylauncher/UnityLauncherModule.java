package com.unitylauncher;

import android.content.Intent;
import android.util.Log;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActivityManager;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Looper;

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

    private BroadcastReceiver unityFinishedReceiver;

    public UnityLauncherModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
        context.addLifecycleEventListener(this);
        
        unityActivityCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                String activityName = activity.getClass().getName();
                Log.d(TAG, "Activity created: " + activityName);
                
                if (activityName.contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity created");
                    isUnityActivityActive = true;
                } else {
                    Log.d(TAG, "React Native activity created: " + activityName);
                }
            }
            
            @Override
            public void onActivityStarted(Activity activity) {
                String activityName = activity.getClass().getName();
                Log.d(TAG, "Activity started: " + activityName);
                
                if (activityName.contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity started");
                } else {
                    Log.d(TAG, "React Native activity started: " + activityName);
                }
            }
            
            @Override
            public void onActivityResumed(Activity activity) {
                String activityName = activity.getClass().getName();
                Log.d(TAG, "Activity resumed: " + activityName);
                
                if (activityName.contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity resumed");
                } else {
                    Log.d(TAG, "React Native activity resumed: " + activityName);
                }
            }
            
            @Override
            public void onActivityPaused(Activity activity) {
                String activityName = activity.getClass().getName();
                Log.d(TAG, "Activity paused: " + activityName);
                
                if (activityName.contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity paused");
                } else {
                    Log.d(TAG, "React Native activity paused: " + activityName);
                }
            }
            
            @Override
            public void onActivityStopped(Activity activity) {
                String activityName = activity.getClass().getName();
                Log.d(TAG, "Activity stopped: " + activityName);
                
                if (activityName.contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity stopped");
                } else {
                    Log.d(TAG, "React Native activity stopped: " + activityName);
                }
            }
            
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                String activityName = activity.getClass().getName();
                Log.d(TAG, "Activity save instance state: " + activityName);
                
                if (activityName.contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity state saved");
                } else {
                    Log.d(TAG, "React Native activity state saved: " + activityName);
                }
            }
            
            @Override
            public void onActivityDestroyed(Activity activity) {
                String activityName = activity.getClass().getName();
                Log.d(TAG, "Activity destroyed: " + activityName);
                
                if (activityName.contains("CustomUnityPlayerActivity")) {
                    Log.d(TAG, "Unity activity destroyed");
                    isUnityActivityActive = false;
                    isUnityRunning = false;
                    
                    if (unityReturnCallback != null) {
                        unityReturnCallback.invoke();
                        unityReturnCallback = null;
                    }
                    
                    System.gc();
                } else {
                    Log.d(TAG, "React Native activity destroyed: " + activityName);
                }
            }
        };
        
        ((Application) reactContext.getApplicationContext()).registerActivityLifecycleCallbacks(unityActivityCallbacks);

        unityFinishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received broadcast from Unity: " + intent.getAction());
                
                if ("com.unitylauncher.UNITY_FINISHED".equals(intent.getAction())) {
                    unityState = UnityState.IDLE;
                    isUnityRunning = false;
                    
                    Log.d(TAG, "Unity finished, trying to return to React Native activity");
                    
                    // Focus the app by bringing existing React Native activity to foreground
                    try {
                        // Try to get the current activity
                        Activity currentActivity = reactContext.getCurrentActivity();
                        
                        if (currentActivity != null) {
                            // If we have a current activity, use it directly instead of launching a new one
                            Log.d(TAG, "Found existing React Native activity: " + currentActivity.getClass().getSimpleName());
                            Intent bringToFrontIntent = new Intent(currentActivity, currentActivity.getClass());
                            bringToFrontIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            currentActivity.startActivity(bringToFrontIntent);
                            Log.d(TAG, "RESUMING existing React Native activity (should preserve state)");
                        } else {
                            // Fallback: use the main launcher intent but with flags to preserve state
                            Log.d(TAG, "No current React Native activity found, using launcher intent");
                            Intent launchIntent = context.getPackageManager()
                                .getLaunchIntentForPackage(context.getPackageName());
                            
                            if (launchIntent != null) {
                                // Use single top to avoid creating multiple instances
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                context.startActivity(launchIntent);
                                Log.d(TAG, "Launched React Native app using launcher intent");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error focusing app: " + e.getMessage());
                    }
                    
                    // Execute callback on main thread
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (unityReturnCallback != null) {
                                unityReturnCallback.invoke();
                                unityReturnCallback = null;
                            }
                        }
                    });
                    
                    System.gc();
                }
            }
        };
        
        IntentFilter filter = new IntentFilter("com.unitylauncher.UNITY_FINISHED");
        reactContext.registerReceiver(unityFinishedReceiver, filter);
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
    
    @ReactMethod
    public void launchUnityWithData(String serverURL, String socketURL, String game, String contentId, com.facebook.react.bridge.ReadableMap additionalData) {
        try {
            if (unityState == UnityState.IDLE || 
                (unityState == UnityState.STOPPING && System.currentTimeMillis() - lastLaunchTime > 500)) {
                unityState = UnityState.LAUNCHING;
                lastLaunchTime = System.currentTimeMillis();
                
                // Launch Unity with data
                Intent intent = new Intent(reactContext, com.unity3d.player.CustomUnityPlayerActivity.class);
                // Use only NEW_TASK flag to start Unity without destroying React Native activity
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                Activity currentActivity = reactContext.getCurrentActivity();
                if (currentActivity != null) {
                    Log.d(TAG, "Launching Unity with data from React Native activity: " + currentActivity.getClass().getSimpleName());
                } else {
                    Log.d(TAG, "Launching Unity with data (no current React Native activity found)");
                }
                
                // Add data as extras to the intent
                intent.putExtra("serverURL", serverURL);
                intent.putExtra("socketURL", socketURL);
                intent.putExtra("game", game);
                intent.putExtra("contentId", contentId);
                
                // Convert additional data to JSON string and pass it as an extra
                if (additionalData != null) {
                    try {
                        String additionalDataJson = additionalData.toString();
                        intent.putExtra("additionalData", additionalDataJson);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing additionalData: " + e.getMessage());
                    }
                }
                
                reactContext.startActivity(intent);
                isUnityRunning = true;
            } else if (unityState == UnityState.PAUSED) {
                // Resume if paused
                bringUnityToForeground();
            } else {
                Log.w(TAG, "Unity launch requested while in state: " + unityState);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error launching Unity with data: " + e.getMessage());
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
        Activity currentActivity = reactContext.getCurrentActivity();
        if (currentActivity != null) {
            Log.d(TAG, "React Native host resumed: " + currentActivity.getClass().getSimpleName());
        } else {
            Log.d(TAG, "React Native host resumed (no activity reference)");
        }
        
        if (isUnityRunning) {
            Log.d(TAG, "React Native host resumed while Unity was running - Unity is now considered finished");
            isUnityRunning = false;
            
            if (unityReturnCallback != null) {
                Log.d(TAG, "Executing Unity return callback from onHostResume");
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
        
        try {
            reactContext.unregisterReceiver(unityFinishedReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
        }
    }

    private void actuallyLaunchUnity() {
        Intent intent = new Intent(reactContext, com.unity3d.player.CustomUnityPlayerActivity.class);
        // Use only NEW_TASK flag to start Unity without destroying React Native activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        Activity currentActivity = reactContext.getCurrentActivity();
        if (currentActivity != null) {
            Log.d(TAG, "Launching Unity from React Native activity: " + currentActivity.getClass().getSimpleName());
        } else {
            Log.d(TAG, "Launching Unity (no current React Native activity found)");
        }
        
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
