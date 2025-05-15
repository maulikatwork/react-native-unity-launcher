# Cursor Rules for react-native-unity-launcher

## Project Structure Overview

- **react-native-unity-launcher**: A React Native package that facilitates launching Unity applications from within React Native apps. Currently targets Android only.
  - `android/`: Contains the Android-specific implementation
  - `index.js`: JavaScript bridge to native modules
  - `index.d.ts`: TypeScript definitions

- **Base Testing (Unity Project)**: Unity project used to build native libraries for iOS and Android.
  - `Assets/Scripts/ReactNativeBridge.cs`: Used for communication between Unity and React Native.

## Development Rules

### General Rules

1. **Project Scope**:
   - Current focus is on Android implementation only.
   - iOS implementation is planned but not started.

2. **Repository Structure**:
   - All changes to React Native integration should be done in the `react-native-unity-launcher` package.
   - Unity-side changes should be done in the Unity project "Base Testing".

### Android Implementation

1. **Manifest Changes**:
   - Do NOT modify the Android manifest in the React Native package.
   - All manifest configurations should be implemented in the Unity project's export settings.

2. **Activity Management**:
   - `CustomUnityPlayerActivity` is referred to in the code but defined in the Unity export.
   - React Native to Unity navigation is via Intent launches.
   - Unity to React Native communication happens through the `ReactNativeBridge.cs` script.

3. **Communication Flow**:
   - React Native → Unity: Launch via Intent to `CustomUnityPlayerActivity`
   - Unity → React Native: `ReactNativeBridge.cs` calls Android-side method to return control

### Unity Implementation

1. **Unity Bridge**:
   - `ReactNativeBridge.cs` is the main communication bridge between Unity and React Native.
   - Unity cleanup occurs before returning to React Native.

2. **Native Library Generation**:
   - All settings for the native library should be configured in the "Base Testing" Unity project.
   - The generated AAR file should be placed in the `android/libs` directory of the React Native package.

### Generated Android Project

1. **Generated Code**:
   - The Android project generated from Unity exports should not be manually modified.
   - It serves as reference only.

## API Guidelines

1. **Public API**:
   - Keep the API surface minimal and focused on launching Unity content.
   - Current methods:
     - `launchUnity()`: Launch Unity without callback
     - `launchUnityWithCallback(callback)`: Launch Unity with a callback when returning to React Native

2. **Native Modules**:
   - Follow React Native's conventions for native module implementation.
   - Ensure proper lifecycle management. 