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
android {
    // ... other android config
    repositories {
        flatDir {
            dirs 'libs'
        }
    }
}

dependencies {
    // ... other dependencies
    implementation project(':react-native-unity-launcher')
    implementation fileTree(dir: 'libs', include: ['*.aar'])
}
```

3. Create a `libs` directory in your `android/app` folder if it doesn't exist already and copy your Unity AAR file into it:

```bash
mkdir -p android/app/libs
# Copy your Unity AAR file to android/app/libs
```

4. Register the package in your `MainApplication`:

#### For Java (MainApplication.java):

```java
import com.mychampx1.unitylauncher.UnityLauncherPackage; // Add this import

@Override
protected List<ReactPackage> getPackages() {
    List<ReactPackage> packages = new PackageList(this).getPackages();
    // Add the Unity Launcher package
    packages.add(new UnityLauncherPackage());
    return packages;
}
```

#### For Kotlin (MainApplication.kt):

```kotlin
import com.mychampx1.unitylauncher.UnityLauncherPackage // Add this import

override fun getPackages(): List<ReactPackage> =
            PackageList(this).packages.apply {
              // Add Unity Package
              add(UnityLauncherPackage())
            }
```

## Usage

### Launching Unity

Simple way to launch Unity:

```javascript
import { launchUnity } from "react-native-unity-launcher";

// Launch Unity
launchUnity();
```

### Launching Unity with Callback

Launch Unity and receive a callback when returning to React Native:

```javascript
import { launchUnityWithCallback } from "react-native-unity-launcher";

// Launch Unity with callback
launchUnityWithCallback(() => {
  console.log("Returned from Unity");
  // Do something after returning from Unity
});
```

### Launching Unity with Data

Launch Unity and pass data that can be accessed from Unity:

```javascript
import { launchUnityWithData } from "react-native-unity-launcher";

// Launch Unity with data
launchUnityWithData(
  "https://your-server.com/api", // serverURL
  "wss://your-socket-server.com", // socketURL
  "game-id", // game
  "content-123", // contentId
  {
    // additionalData (optional)
    userId: "user123",
    settings: {
      difficulty: "hard",
      theme: "dark",
    },
  }
);
```

### Launching Unity with Data and Callback

Launch Unity with data and receive a callback when returning to React Native:

```javascript
import { launchUnityWithDataCallback } from "react-native-unity-launcher";

// Launch Unity with data and callback
launchUnityWithDataCallback(
  "https://your-server.com/api", // serverURL
  "wss://your-socket-server.com", // socketURL
  "auth-token-123", // token
  "game-id", // game
  "match-123", // matchId
  {
    // additionalData (optional)
    userId: "user123",
    settings: {
      difficulty: "hard",
      theme: "dark",
    },
  },
  () => {
    console.log("Returned from Unity");
    // Do something after returning from Unity
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

## iOS Integration

For iOS, you need to integrate the Unity framework with your React Native project. Follow these steps:

### 1. Build Unity iOS Framework

1. Open your Unity project
2. Go to **File > Build Settings**
3. Switch platform to **iOS**
4. Enable **Create Xcode Project** option
5. Set **Target SDK** to **Device SDK**
6. Click **Build**
7. Select destination folder for the Xcode project

### 2. Generate the UnityFramework.framework

1. Open the generated Xcode project
2. In the scheme selector, choose **UnityFramework** (not Unity-iPhone)
3. Set build configuration to **Release**
4. Select **Generic iOS Device** as the destination
5. Go to **Product > Build** (or press ⌘B)
6. After successful build, go to **Product > Show Build Folder in Finder**
7. Navigate to **Products/Release-iphoneos/** folder
8. Find **UnityFramework.framework** there

### 3. Add Framework to React Native iOS Project

1. Create a **Frameworks** directory in your React Native app's iOS folder:

   ```bash
   mkdir -p YourReactNativeApp/ios/Frameworks
   ```

2. Copy the Unity framework to this directory:

   ```bash
   cp -R /path/to/UnityFramework.framework YourReactNativeApp/ios/Frameworks/
   ```

3. If needed, add the framework to your .gitignore:
   ```
   # Add to .gitignore if framework is large
   ios/Frameworks/UnityFramework.framework/
   ```

### 4. Update Your React Native Project's Podfile

Modify your main React Native app's Podfile to include the Unity framework. Due to potential issues with framework paths containing spaces, we'll use a script phase approach instead of directly linking the framework:

#### For Standard React Native Projects

```ruby
target 'YourAppName' do
  # Standard React Native pods
  config = use_native_modules!
  use_react_native!(
    :path => config[:reactNativePath],
    :hermes_enabled => true,
    # Other React Native flags...
  )

  # Add the Unity Launcher
  pod 'react-native-unity-launcher', :path => '../node_modules/react-native-unity-launcher'

  # Add a script phase to copy the framework at build time
  script_phase :name => 'Copy Unity Framework',
               :execution_position => :before_compile,
               :script => <<-SCRIPT
                 mkdir -p "${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}"
                 cp -R "${PROJECT_DIR}/Frameworks/UnityFramework.framework" "${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}/"
               SCRIPT

  # Your other pods...
end

# Add this post_install hook
post_install do |installer|
  # Standard React Native post install
  react_native_post_install(installer)

  # Unity Framework specific configurations
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      # Disable bitcode for all targets
      config.build_settings['ENABLE_BITCODE'] = 'NO'
    end
  end
end
```

#### For Projects Using New Architecture

```ruby
target 'YourAppName' do
  # Standard React Native pods
  config = use_native_modules!
  use_react_native!(
    :path => config[:reactNativePath],
    :hermes_enabled => true,
    :fabric_enabled => true,  # New architecture enabled
    # Other React Native flags...
  )

  # Add the Unity Launcher
  pod 'react-native-unity-launcher', :path => '../node_modules/react-native-unity-launcher'

  # Add a script phase to copy the framework at build time
  script_phase :name => 'Copy Unity Framework',
               :execution_position => :before_compile,
               :script => <<-SCRIPT
                 mkdir -p "${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}"
                 cp -R "${PROJECT_DIR}/Frameworks/UnityFramework.framework" "${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}/"
               SCRIPT
end

# Keep the standard post_install plus Unity configurations
post_install do |installer|
  react_native_post_install(installer)
  __apply_Xcode_12_5_M1_post_install_workaround(installer)

  # Unity Framework specific configurations
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['ENABLE_BITCODE'] = 'NO'
    end
  end
end
```

#### For Projects Using Use_Frameworks!

If your project uses `use_frameworks!` (common with Swift dependencies):

```ruby
platform :ios, min_ios_version_supported
prepare_react_native_project!

# Detect linkage type from environment
linkage = ENV['USE_FRAMEWORKS']
if linkage != nil
  Pod::UI.puts "Configuring Pod with #{linkage}ally linked Frameworks".green
  use_frameworks! :linkage => linkage.to_sym
end

target 'YourAppName' do
  config = use_native_modules!
  use_react_native!(
    :path => config[:reactNativePath],
    :app_path => "#{Pod::Config.instance.installation_root}/.."
  )

  # Add Unity Launcher INSIDE the target block
  pod 'react-native-unity-launcher', :path => '../node_modules/react-native-unity-launcher'

  # Add a script phase to copy the framework at build time
  script_phase :name => 'Copy Unity Framework',
               :execution_position => :before_compile,
               :script => <<-SCRIPT
                 mkdir -p "${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}"
                 cp -R "${PROJECT_DIR}/Frameworks/UnityFramework.framework" "${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}/"
               SCRIPT

  # Your other pods...
end

post_install do |installer|
  # Standard React Native post install
  react_native_post_install(
    installer,
    config[:reactNativePath],
    :mac_catalyst_enabled => false
  )

  # Unity Framework specific configurations - may not be needed with script approach
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['ENABLE_BITCODE'] = 'NO'
    end
  end
end
```

#### Important Notes About Podfile Configuration

1. The script phase approach avoids issues with spaces in paths and complex podspec files
2. Make sure the path to your UnityFramework.framework is correct in the script phase
3. The script will copy the framework to the correct location at build time
4. No podspec file is required with this approach
5. Always include the script phase inside your target block
6. Always keep the standard React Native post_install hooks

### 5. Update Info.plist

Add these settings to your app's `Info.plist`:

```xml
<key>UIRequiresFullScreen</key>
<true/>
<key>UIViewControllerBasedStatusBarAppearance</key>
<false/>
<key>UIRequiredDeviceCapabilities</key>
<array>
  <string>arm64</string>
</array>
<key>NSCameraUsageDescription</key>
<string>Used for AR content</string>
```

### 6. Install Pods

Run pod install to integrate everything:

```bash
cd ios
pod install
```

### 7. Verify Integration in Xcode

1. Open your app's `.xcworkspace` file in Xcode
2. Check that the script phase appears in your target's Build Phases
3. Build the project to verify there are no linking errors
4. After building, check that UnityFramework.framework appears in the built products directory

### Troubleshooting

If you encounter issues:

1. **Framework not copied at build time:**

   - Verify the path in the script phase is correct
   - Check that the UnityFramework.framework exists in your Frameworks directory
   - Try a clean build (Product > Clean Build Folder)

2. **Linking errors:**

   - Make sure your app's deployment target matches or is higher than Unity's minimum iOS version (usually iOS 11.0+)
   - Unity uses IL2CPP by default which requires arm64 architecture - make sure your app supports arm64

3. **Runtime crashes:**

   - Unity requires full screen mode - ensure UIRequiresFullScreen is set to true in Info.plist
   - Check Unity's logs in the Xcode console for specific error messages

4. **Black screen when launching Unity:**

   - Verify your device supports the required Metal version
   - Enable Metal API validation in Xcode's scheme editor for more detailed errors

5. **Memory issues:**

   - Unity consumes significant memory - implement proper lifecycle handling to release resources when Unity view is dismissed

6. **Spaces in path issues:**
   - If you have spaces in your project path, you may need to adjust the script phase with proper escaping
   - Try using double quotes around paths in the script
