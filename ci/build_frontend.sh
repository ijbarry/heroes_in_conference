#!/bin/bash
project="frontend"

echo "Attempting to build $project for Windows..."
mkdir -p "$(pwd)/Build/windows"
/Applications/Unity/Unity.app/Contents/MacOS/Unity \
  -batchmode \
  -nographics \
  -silent-crashes \
  -logFile \
  -logFile "$(pwd)/unity.log" \
  -buildWindowsPlayer "$(pwd)/Build/windows/$project.exe" \
  -quit

echo "Attempting to build $project for OS X..."
mkdir -p "$(pwd)/Build/osx"
/Applications/Unity/Unity.app/Contents/MacOS/Unity \
  -batchmode \
  -nographics \
  -silent-crashes \
  -logFile "$(pwd)/unity.log" \
  -projectPath $(pwd) \
  -buildOSXUniversalPlayer "$(pwd)/Build/osx/$project.app" \
  -quit

echo "Attempting to build $project for Linux..."
mkdir -p "$(pwd)/Build/linux"
/Applications/Unity/Unity.app/Contents/MacOS/Unity \
  -batchmode \
  -nographics \
  -silent-crashes \
  -logFile "$(pwd)/unity.log" \
  -projectPath $(pwd) \
  -buildLinuxUniversalPlayer "$(pwd)/Build/linux/$project" \
  -quit

echo 'Attempting to zip builds...'
zip -r $(pwd)/Build/linux.zip $(pwd)/Build/linux/
zip -r $(pwd)/Build/mac.zip $(pwd)/Build/osx/
zip -r $(pwd)/Build/windows.zip $(pwd)/Build/windows/

echo 'Unity build log'
cat "$(pwd)/unity.log"
