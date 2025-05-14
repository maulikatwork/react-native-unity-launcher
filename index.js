import { NativeModules } from "react-native";

const { UnityLauncher } = NativeModules;

export function launchUnity() {
  return UnityLauncher.launchUnity();
}

export function launchUnityWithCallback(callback) {
  return UnityLauncher.launchUnityWithCallback(callback);
}
