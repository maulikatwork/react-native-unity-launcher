import { NativeModules } from "react-native";

const { UnityLauncher } = NativeModules;

export function launchUnity() {
  return UnityLauncher.launchUnity();
}

export function launchUnityWithCallback(callback) {
  return UnityLauncher.launchUnityWithCallback(callback);
}

export function launchUnityWithData(
  serverURL,
  socketURL,
  game,
  contentId,
  additionalData = {}
) {
  return UnityLauncher.launchUnityWithData(
    serverURL,
    socketURL,
    game,
    contentId,
    additionalData
  );
}
