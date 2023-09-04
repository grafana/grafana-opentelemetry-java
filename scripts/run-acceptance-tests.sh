#!/usr/bin/env bash

set -euo pipefail

go install github.com/onsi/ginkgo/v2/ginkgo
git clone git@github.com:grafana/oats.git

cd oats
export TESTCASE_SKIP_BUILD=true
export TESTCASE_BASE_PATH=..
ginkgo -v -r
