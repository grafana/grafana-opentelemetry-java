#!/usr/bin/env bash

set -euo pipefail

newVersion=${TAG#v}
if [ -z "$newVersion" ]; then
	echo "TAG is missing"
	exit 1
fi

if [[ ! "$newVersion" =~ ^2\.[0-9]+\.[0-9]+(\.[0-9]+)?$ ]]; then
	echo "new version $newVersion is not valid - new version should be in the format of x.y.z.g?"
	exit 1
fi

# update version in build.gradle, it's in the second line
sed -i "2s/.*/version '$newVersion'/" build.gradle

./gradlew assemble
