#!/usr/bin/env bash

set -euo pipefail

tag=$1
newVersion=${tag#v}
if [ -z "$newVersion" ]; then
  echo "new version is missing"
  exit 1
fi

if [[ ! "$newVersion" =~ ^2\.[0-9]+\.[0-9]+(\.[0-9]+)?$ ]]; then
  echo "new version $newVersion is not valid - new version should be in the format of x.y.z.g?"
  exit 1
fi

oldVersion=$(grep -oP "(?<=^version )(.*)" build.gradle | sed "s/'//g")
sed -i "s/$oldVersion/$newVersion/g" examples/run-example.sh Dockerfile Dockerfile.production
sed -i "s/^version '$oldVersion'/version '$newVersion'/g" build.gradle
