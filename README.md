# React Native Unity Launcher

A React Native package for launching Unity applications from within React Native apps. Currently targets Android only.

## Installation

```bash
npm install react-native-unity-launcher --save
```

## Usage

### Launching Unity

Simple way to launch Unity:

```javascript
import { launchUnity } from 'react-native-unity-launcher';

// Launch Unity
launchUnity();
```

### Launching Unity with Callback

Launch Unity and receive a callback when returning to React Native:

```javascript
import { launchUnityWithCallback } from 'react-native-unity-launcher';

// Launch Unity with callback
launchUnityWithCallback(() => {
  console.log('Returned from Unity');
  // Do something after returning from Unity
});
```

### Launching Unity with Data

Launch Unity and pass data that can be accessed from Unity:

```javascript
import { launchUnityWithData } from 'react-native-unity-launcher';

// Launch Unity with data
launchUnityWithData(
  'https://your-server.com/api',  // serverURL
  'wss://your-socket-server.com', // socketURL
  'auth-token-xyz',               // token
  'game-id',                      // game
  'content-123',                  // contentId
  {                               // additionalData (optional)
    userId: 'user123',
    settings: {
      difficulty: 'hard',
      theme: 'dark'
    }
  }
);
```

## Unity Integration

### Setting up ReactNativeBridge in Unity

1. Create a C# script in Unity named `ReactNativeBridge.cs` in your Assets/Scripts folder:

```csharp
using UnityEngine;
using System;
using System.Collections;

#if UNITY_ANDROID
using UnityEngine.Android;
#endif

public class ReactNativeBridge : MonoBehaviour
{
    private static ReactNativeBridge instance;

    // Data from React Native
    public string ServerURL { get; private set; }
    public string SocketURL { get; private set; }
    public string Token { get; private set; }
    public string Game { get; private set; }
    public string ContentId { get; private set; }
    public string AdditionalData { get; private set; }

    public static ReactNativeBridge Instance
    {
        get
        {
            if (instance == null)
            {
                GameObject go = new GameObject("ReactNativeBridge");
                instance = go.AddComponent<ReactNativeBridge>();
                DontDestroyOnLoad(go);
            }
            return instance;
        }
    }

    void Awake()
    {
        if (instance != null && instance != this)
        {
            Destroy(gameObject);
            return;
        }

        instance = this;
        DontDestroyOnLoad(gameObject);
        
        // Get data from intent extras
        GetDataFromIntent();
    }

    void GetDataFromIntent()
    {
#if UNITY_ANDROID
        try
        {
            using (AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
            {
                using (AndroidJavaObject activity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
                {
                    using (AndroidJavaObject intent = activity.Call<AndroidJavaObject>("getIntent"))
                    {
                        AndroidJavaObject extras = intent.Call<AndroidJavaObject>("getExtras");
                        if (extras != null)
                        {
                            // Get the data passed from React Native
                            if (extras.Call<bool>("containsKey", "serverURL"))
                                ServerURL = extras.Call<string>("getString", "serverURL");
                            
                            if (extras.Call<bool>("containsKey", "socketURL"))
                                SocketURL = extras.Call<string>("getString", "socketURL");
                            
                            if (extras.Call<bool>("containsKey", "token"))
                                Token = extras.Call<string>("getString", "token");
                            
                            if (extras.Call<bool>("containsKey", "game"))
                                Game = extras.Call<string>("getString", "game");
                            
                            if (extras.Call<bool>("containsKey", "contentId"))
                                ContentId = extras.Call<string>("getString", "contentId");
                            
                            if (extras.Call<bool>("containsKey", "additionalData"))
                                AdditionalData = extras.Call<string>("getString", "additionalData");
                            
                            Debug.Log("Data received from React Native: " + 
                                      "ServerURL=" + ServerURL + ", " +
                                      "SocketURL=" + SocketURL + ", " +
                                      "Token=" + Token + ", " +
                                      "Game=" + Game + ", " +
                                      "ContentId=" + ContentId);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Debug.LogError("Error getting data from intent: " + e.Message);
        }
#endif
    }

    // Parse the additionalData JSON string to a dictionary or custom object
    public T ParseAdditionalData<T>()
    {
        if (string.IsNullOrEmpty(AdditionalData))
            return default(T);
            
        try
        {
            return JsonUtility.FromJson<T>(AdditionalData);
        }
        catch (Exception e)
        {
            Debug.LogError("Error parsing additional data: " + e.Message);
            return default(T);
        }
    }

    // Return to React Native
    public void ReturnToReactNative()
    {
#if UNITY_ANDROID
        try
        {
            using (AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
            {
                using (AndroidJavaObject activity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
                {
                    // Notify React Native that Unity is finishing
                    using (AndroidJavaObject context = activity.Call<AndroidJavaObject>("getApplicationContext"))
                    {
                        // Create a broadcast intent
                        using (AndroidJavaObject intent = new AndroidJavaObject("android.content.Intent", "com.unitylauncher.UNITY_FINISHED"))
                        {
                            context.Call("sendBroadcast", intent);
                        }
                    }
                    
                    // Finish Unity activity
                    activity.Call("finish");
                }
            }
        }
        catch (Exception e)
        {
            Debug.LogError("Error returning to React Native: " + e.Message);
        }
#endif
    }
}
```

2. Attach this script to a GameObject in your Unity scene.

3. Access the data in your Unity scripts:

```csharp
using UnityEngine;

public class MyGameController : MonoBehaviour
{
    void Start()
    {
        // Access the data from React Native
        string serverURL = ReactNativeBridge.Instance.ServerURL;
        string socketURL = ReactNativeBridge.Instance.SocketURL;
        string token = ReactNativeBridge.Instance.Token;
        string game = ReactNativeBridge.Instance.Game;
        string contentId = ReactNativeBridge.Instance.ContentId;
        
        // Parse additional data if needed
        AdditionalDataType additionalData = ReactNativeBridge.Instance.ParseAdditionalData<AdditionalDataType>();
        
        Debug.Log("Starting game with data: " + 
                  "serverURL=" + serverURL + ", " +
                  "token=" + token + ", " +
                  "game=" + game);
                  
        // Use the data in your game logic
    }
    
    // Example class for parsing additionalData
    [System.Serializable]
    public class AdditionalDataType
    {
        public string userId;
        public Settings settings;
    }
    
    [System.Serializable]
    public class Settings
    {
        public string difficulty;
        public string theme;
    }
    
    // Return to React Native when done
    public void GoBackToReactNative()
    {
        ReactNativeBridge.Instance.ReturnToReactNative();
    }
}
```

## Notes

- This package is currently Android-only.
- iOS support is planned for future releases.
- All Unity-side changes should be done in your Unity project.
- The Unity project should export the proper Android library for integration.

## License

MIT
