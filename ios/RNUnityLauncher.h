#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RNUnityLauncher : RCTEventEmitter <RCTBridgeModule>

// Property to store callback for Unity finish
@property (nonatomic, copy) RCTResponseSenderBlock unityFinishedCallback;

@end
