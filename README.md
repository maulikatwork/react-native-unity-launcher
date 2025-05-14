# React Native Unity Launcher

A React Native package that allows seamless integration and launching of Unity games/apps within React Native applications. This package includes the necessary configurations and AAR files to launch Unity from your React Native app without requiring manual Unity exports.

## Installation

```sh
# Using npm with git URL
npm install git+https://github.com/maulikatwork/react-native-unity-launcher.git

# Using yarn with git URL
yarn add git+https://github.com/maulikatwork/react-native-unity-launcher.git
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

```javascript
import { UnityLauncher } from 'react-native-unity-launcher';

// Launch Unity
UnityLauncher.launchUnity();
```

## API Reference

Based on the provided code, here are the available methods:

| Method | Description | Parameters |
|--------|-------------|------------|
| `launchUnity()` | Launch the Unity player activity | None |

## How It Works

The package includes:

1. **UnityLauncherModule.java**: Contains the native Android module that provides the `launchUnity()` method, which creates and starts an Intent to launch the Unity Player Activity.

2. **UnityLauncherPackage.java**: Implements the ReactPackage interface to register the UnityLauncherModule with React Native.

3. **AndroidManifest.xml**: Defines the Unity Player Activity with appropriate configurations:
   - Full-screen theme
   - Single task launch mode
   - Landscape orientation

No additional Unity AAR files or manual configuration is required as they are bundled with the package.

## Troubleshooting

### Common Issues

1. **Unity activity not found**
   - Ensure the package is properly linked in your Android project
   - Check that the Unity Player Activity is correctly defined in the manifest

2. **Black screen when launching Unity**
   - Verify that your device supports the required OpenGL version
   - Check Unity-specific logs in the Android Logcat

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

MIT
