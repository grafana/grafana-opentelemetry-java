#!/usr/bin/env bash

newVersion=$1
if [ -z "$newVersion" ]; then
  echo "new version is missing"
  exit 1
fi

res="${newVersion//[^.]}"
if [ "${#res}" -ne 2 ]; then
  echo "new version $newVersion is not valid - new version should be in the format of x.y.z"
  exit 1
fi

oldVersion=$(grep -oP "(?<=^version )(.*)" build.gradle | sed "s/'//g")
sed -i "s/$oldVersion/$newVersion/g" examples/run-example.sh Dockerfile
sed -i "s/^version '$oldVersion'/version '$newVersion'/g" build.gradle
