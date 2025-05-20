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
  
  # Set search paths for UnityFramework
  s.xcconfig = {
    'FRAMEWORK_SEARCH_PATHS' => '$(inherited) "$(SRCROOT)/../../ios/Frameworks"',
    'HEADER_SEARCH_PATHS' => '$(inherited) "$(SRCROOT)/../../ios/Frameworks/UnityFramework.framework/Headers"'
  }
  
  # Instructions for installing the Unity framework
  s.prepare_command = <<-CMD
    echo "📱 React Native Unity Launcher: Unity Framework integration"
    echo "Place UnityFramework.framework in your iOS app's 'ios/Frameworks' directory"
  CMD
end 