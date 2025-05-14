package com.unitylauncher;

import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class UnityLauncherModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private static Callback unityReturnCallback;

    public UnityLauncherModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
    }

    @Override
    public String getName() {
        return "UnityLauncher";
    }

    @ReactMethod
    public void launchUnity() {
        Intent intent = new Intent(reactContext, com.unity3d.player.CustomUnityPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(intent);
    }
    
    @ReactMethod
    public void launchUnityWithCallback(Callback callback) {
        unityReturnCallback = callback;
        Intent intent = new Intent(reactContext, com.unity3d.player.CustomUnityPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(intent);
    }
    
    // Static method to be called from the Activity.onDestroy
    public static void onUnityReturn() {
        if (unityReturnCallback != null) {
            unityReturnCallback.invoke();
            unityReturnCallback = null;
        }
    }
}
