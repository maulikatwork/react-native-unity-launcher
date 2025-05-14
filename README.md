# React Native Unity Launcher

A React Native package that allows seamless integration and launching of Unity games/apps within React Native applications.

## Installation

```sh
# Using npm with git URL
npm install git+https://github.com/maulikatwork/react-native-unity-launcher.git

# Using yarn with git URL
yarn add git+https://github.com/maulikatwork/react-native-unity-launcher.git

# Or if you want a specific branch/tag/commit
npm install git+https://github.com/maulikatwork/react-native-unity-launcher.git#branch-name
yarn add git+https://github.com/maulikatwork/react-native-unity-launcher.git#branch-name
```

## Prerequisites

- React Native project (>= 0.60.0)
- Unity project (exported for mobile)

## Setup

### Android Setup

1. Export your Unity project as an Android Studio project
2. Copy the exported Unity project's `unityLibrary` folder into your React Native project's `android` directory
3. In your React Native project's `android/settings.gradle`, add:

```gradle
include ':unityLibrary'
```

4. In your app level `build.gradle` file, add the Unity library as a dependency:

```gradle
dependencies {
    // ... other dependencies
    implementation project(':unityLibrary')
}
```

## API Reference

### Methods

| Method | Description | Parameters |
|--------|-------------|------------|
| `launchUnity()` | Launch the Unity app/game | None |
| `launchUnityScene(sceneName)` | Launch Unity with a specific scene | `sceneName`: String |
| `sendMessageToUnity(gameObject, method, message)` | Send a message to a Unity GameObject | `gameObject`: String, `method`: String, `message`: String |
| `addUnityMessageListener(callback)` | Add a listener for messages from Unity | `callback`: Function |
| `removeUnityMessageListener(callback)` | Remove a message listener | `callback`: Function |
| `closeUnity()` | Close the Unity app/game | None |

## Example

See the `example` folder for a sample implementation.

## Advanced Configuration

### Custom Unity Activity (Android)

If you need to use a custom Unity Activity:

```java
// In your MainActivity.java
import com.reactnativeunitylauncher.UnityLauncherModule;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    UnityLauncherModule.setCustomUnityActivityClass(YourCustomUnityActivity.class);
}
```

### Performance Considerations

- Unity can be resource-intensive. Consider managing the lifecycle properly.
- Use `closeUnity()` when you no longer need the Unity view to free up resources.

## Troubleshooting

### Common Issues

1. **Unity crashes on launch**
   - Verify that Unity export settings are correct
   - Ensure all required libraries are linked

2. **Communication not working**
   - Check GameObject names and method names
   - Verify Unity side has proper message handlers

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

MIT
