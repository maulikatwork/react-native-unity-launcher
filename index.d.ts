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

  /**
   * Launches the Unity application with data that will be passed to Unity
   * @param serverURL Server URL string to pass to Unity
   * @param socketURL Socket URL string to pass to Unity
   * @param token Authentication token string to pass to Unity
   * @param game Game identifier string to pass to Unity
   * @param matchId Match identifier string to pass to Unity
   * @param additionalData Optional JSON object with additional data to pass to Unity
   * @returns The return value from the native Unity launcher module
   */
  export function launchUnityWithData(
    serverURL: string,
    socketURL: string,
    token: string,
    game: string,
    matchId: string,
    additionalData?: Record<string, any>
  ): any;
} 