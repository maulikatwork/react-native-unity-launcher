require 'json'
package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "react-native-unity-launcher"
  s.version      = package['version']
  s.summary      = "React Native wrapper for launching Unity projects"
  
  s.homepage     = package['repository']['url']
  s.license      = { :type => "MIT" }
  s.author       = { "React Native Community" => "rnc@github.com" }
  
  s.platform     = :ios, "11.0"
  s.source       = { :git => package['repository']['url'], :tag => "v#{s.version}" }
  s.source_files = "ios/**/*.{h,m,mm}"
  
  s.requires_arc = true
  
  s.dependency "React-Core"
  
  # Look for UnityFramework in the parent project
  s.frameworks = 'UnityFramework'
  
  # Preserve paths for UnityFramework
  s.preserve_paths = 'UnityFramework.framework'
  
  # Set search paths for UnityFramework
  s.xcconfig = {
    'FRAMEWORK_SEARCH_PATHS' => '$(inherited) "$(SRCROOT)/../../ios/Frameworks" "$(PODS_ROOT)/../../react-native-unity-launcher"',
    'HEADER_SEARCH_PATHS' => '$(inherited) "$(SRCROOT)/../../ios/Frameworks/UnityFramework.framework/Headers"'
  }
  
  # Setup and copy UnityFramework from parent project
  s.prepare_command = <<-CMD
    echo "📱 React Native Unity Launcher: Setting up Unity Framework..."
    FRAMEWORK_PATH="../../ios/Frameworks/UnityFramework.framework"
    DESTINATION_PATH="./UnityFramework.framework"
    
    if [ -d "$FRAMEWORK_PATH" ]; then
      echo "Found UnityFramework at $FRAMEWORK_PATH"
      if [ -d "$DESTINATION_PATH" ]; then
        echo "Removing existing copy of UnityFramework"
        rm -rf "$DESTINATION_PATH"
      fi
      echo "Copying UnityFramework to package directory"
      cp -R "$FRAMEWORK_PATH" "$DESTINATION_PATH"
      echo "✅ UnityFramework copied successfully"
    else
      echo "⚠️ UnityFramework not found at $FRAMEWORK_PATH"
      echo "Please ensure UnityFramework.framework exists in your iOS app's 'ios/Frameworks' directory"
    fi
  CMD
end 