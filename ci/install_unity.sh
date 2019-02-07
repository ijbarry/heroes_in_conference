#!/bin/bash
UNITY_DOWNLOAD_CACHE="$(pwd)/cache"
BASE_URL=https://netstorage.unity3d.com/unity
HASH=393bae82dbb8
VERSION=2018.3.3f1

download() {
  URL=$BASE_URL/$HASH/$1
	FILE=`basename "$URL"`
	if [ ! -e $UNITY_DOWNLOAD_CACHE/`basename "$URL"` ] ; then
		echo "$FILE does not exist. Downloading from $URL: "
		mkdir -p "$UNITY_DOWNLOAD_CACHE"
		curl -o $UNITY_DOWNLOAD_CACHE/`basename "$URL"` "$URL"
	else
		echo "$FILE Exists. Skipping download."
  fi
}

install() {
  PACKAGE=$1
	download $1
	echo "Installing `basename "$PACKAGE"`"
  sudo installer -dumplog -package $UNITY_DOWNLOAD_CACHE/`basename "$PACKAGE"` -target /
}

echo "Contents of Unity Download cache:"
ls $UNITY_DOWNLOAD_CACHE

echo "Installing Unity..."
install "MacEditorInstaller/Unity-$VERSION.pkg"
install "MacEditorTargetInstaller/UnitySetup-Windows-Support-for-Editor-$VERSION.pkg"
install "MacEditorTargetInstaller/UnitySetup-Mac-Support-for-Editor-$VERSION.pkg"
install "MacEditorTargetInstaller/UnitySetup-Linux-Support-for-Editor-$VERSION.pkg"
