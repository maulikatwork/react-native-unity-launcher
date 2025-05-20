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
  
  # This podspec doesn't include the Unity framework
  # The parent app is expected to include it separately
  
  # Post install hook to provide setup instructions
  s.prepare_command = <<-CMD
    echo "⚠️  The UnityFramework.framework must be added to your main app's Podfile manually"
    echo "Please see README.md for instructions on adding Unity framework to your project"
  CMD
  
  # To help autolink find the correct header files
  s.pod_target_xcconfig = {
    "HEADER_SEARCH_PATHS" => "\"$(PODS_TARGET_SRCROOT)/ios\""
  }
end 