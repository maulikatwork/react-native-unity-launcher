#import "RNUnityLauncher.h"
#import <UnityFramework/UnityFramework.h>

@implementation RNUnityLauncher

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
  return @[@"UnityFinished"];
}

RCT_EXPORT_METHOD(launchUnity:(NSDictionary *)params 
                  resolver:(RCTPromiseResolveBlock)resolve 
                  rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_main_queue(), ^{
    // Store params in UserDefaults for Unity to read
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:nil];
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    [userDefaults setObject:jsonString forKey:@"UnityLaunchParams"];
    [userDefaults synchronize];
    
    // Present Unity view controller
    [self launchUnityViewController];
    
    // Setup notification observer for when Unity exits
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(unityDidFinish:) 
                                                 name:@"com.mybattle11.unitylauncher.UNITY_FINISHED" 
                                               object:nil];
    
    resolve(@{@"success": @YES});
  });
}

RCT_EXPORT_METHOD(launchUnityWithDataCallback:(NSString *)serverURL
                  socketURL:(NSString *)socketURL
                  token:(NSString *)token
                  game:(NSString *)game
                  matchId:(NSString *)matchId
                  additionalData:(NSDictionary *)additionalData
                  callback:(RCTResponseSenderBlock)callback) {
  
  dispatch_async(dispatch_get_main_queue(), ^{
    // Create params dictionary
    NSDictionary *params = @{
      @"serverURL": serverURL ?: @"",
      @"socketURL": socketURL ?: @"",
      @"token": token ?: @"",
      @"game": game ?: @"",
      @"matchId": matchId ?: @"",
      @"additionalData": [self jsonStringFromObject:additionalData] ?: @"{}"
    };
    
    // Store params in UserDefaults for Unity to read
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:nil];
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    [userDefaults setObject:jsonString forKey:@"UnityLaunchParams"];
    [userDefaults synchronize];
    
    // Store callback
    self.unityFinishedCallback = callback;
    
    // Present Unity view controller
    [self launchUnityViewController];
    
    // Setup notification observer for when Unity exits
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                            selector:@selector(unityDidFinishWithCallback:) 
                                                name:@"com.mybattle11.unitylauncher.UNITY_FINISHED" 
                                              object:nil];
  });
}

- (NSString *)jsonStringFromObject:(id)object {
  if (!object) return nil;
  
  NSError *error;
  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:object options:0 error:&error];
  
  if (error) {
    NSLog(@"Error converting to JSON: %@", error);
    return @"{}";
  }
  
  return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

- (void)launchUnityViewController {
  UIViewController *rootViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
  
  // Load and initialize Unity Framework
  UnityFramework *unityFramework = [self getUnityFramework];
  
  if (unityFramework) {
    // Create and present Unity view controller
    UIViewController *unityViewController = unityFramework.appController.rootViewController;
    
    if (unityViewController) {
      [rootViewController presentViewController:unityViewController animated:YES completion:nil];
    } else {
      NSLog(@"Failed to get Unity view controller");
    }
  } else {
    NSLog(@"Failed to load Unity framework");
  }
}

- (UnityFramework *)getUnityFramework {
  NSString *bundlePath = [[NSBundle mainBundle] bundlePath];
  NSString *frameworkPath = [bundlePath stringByAppendingPathComponent:@"Frameworks/UnityFramework.framework"];
  NSBundle *bundle = [NSBundle bundleWithPath:frameworkPath];
  
  if ([bundle isLoaded] == NO) {
    [bundle load];
  }
  
  UnityFramework *framework = [bundle.principalClass getInstance];
  if (framework.appController == nil) {
    // Initialize Unity
    [framework setExecuteHeader:&_mh_execute_header];
  }
  
  return framework;
}

- (void)unityDidFinish:(NSNotification *)notification {
  dispatch_async(dispatch_get_main_queue(), ^{
    // Get result data if available
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSString *resultData = [userDefaults stringForKey:@"UnityResultData"];
    
    // Send event to JS
    if (resultData) {
      [self sendEventWithName:@"UnityFinished" body:@{@"resultData": resultData}];
    } else {
      [self sendEventWithName:@"UnityFinished" body:@{}];
    }
    
    // Clean up
    [userDefaults removeObjectForKey:@"UnityResultData"];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"com.mybattle11.unitylauncher.UNITY_FINISHED" object:nil];
  });
}

- (void)unityDidFinishWithCallback:(NSNotification *)notification {
  dispatch_async(dispatch_get_main_queue(), ^{
    if (self.unityFinishedCallback) {
      // Get result data if available
      NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
      NSString *resultData = [userDefaults stringForKey:@"UnityResultData"];
      
      // Call the callback with result
      if (resultData) {
        self.unityFinishedCallback(@[@{@"resultData": resultData}]);
      } else {
        self.unityFinishedCallback(@[@{}]);
      }
      
      // Clean up
      self.unityFinishedCallback = nil;
      [userDefaults removeObjectForKey:@"UnityResultData"];
      [[NSNotificationCenter defaultCenter] removeObserver:self name:@"com.mybattle11.unitylauncher.UNITY_FINISHED" object:nil];
    }
  });
}

@end
