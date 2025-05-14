declare module 'react-native-unity-launcher' {
  /**
   * Launches the Unity application
   * @returns The return value from the native Unity launcher module
   */
  export function launchUnity(): any;

  /**
   * Launches the Unity application with a callback that will be invoked when returning to React Native
   * @param callback Function to execute when Unity returns to React Native
   * @returns The return value from the native Unity launcher module
   */
  export function launchUnityWithCallback(callback: () => void): any;
} 