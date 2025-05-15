# React Native Unity Launcher

A React Native package for launching Unity applications from within React Native apps. Currently targets Android only.

## Installation

```bash
npm install github:maulikatwork/react-native-unity-launcher
```

You can also install a specific branch or commit:

```bash
npm install github:maulikatwork/react-native-unity-launcher#branch-name
npm install github:maulikatwork/react-native-unity-launcher#commit-hash
```

## Setup

### Android Setup

1. Add the Unity Launcher package to your React Native project's `android/settings.gradle`:

```gradle
include ':react-native-unity-launcher'
project(':react-native-unity-launcher').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-unity-launcher/android')
```

2. In your app level `android/app/build.gradle`, add the Unity Launcher as a dependency:

```gradle
dependencies {
    // ... other dependencies
    implementation project(':react-native-unity-launcher')
}
```

3. Register the package in your `MainApplication.java`:

```java
import com.unitylauncher.UnityLauncherPackage; // Add this import

@Override
protected List<ReactPackage> getPackages() {
    List<ReactPackage> packages = new PackageList(this).getPackages();
    // Add the Unity Launcher package
    packages.add(new UnityLauncherPackage());
    return packages;
}
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

## Troubleshooting

### Common Issues

1. **Unity activity not found**
   - Ensure the package is properly linked in your Android project
   - Check that the Unity Player Activity is correctly defined in the manifest

2. **Black screen when launching Unity**
   - Verify that your device supports the required OpenGL version
   - Check Unity-specific logs in the Android Logcat

3. **Could not find :unity-export: dependency error**
   - This error occurs when the Android build system cannot locate the Unity AAR file
   - Make sure you're using the latest version of the package which should include the file in the libs directory
   - If the error persists, you can also add your Unity app's AAR file manually to the `node_modules/react-native-unity-launcher/android/libs` directory

## Notes

- This package is currently Android-only.
- iOS support is planned for future releases.
- All Unity-side changes should be done in your Unity project.
- The Unity project should export the proper Android library for integration.

## License

MIT
