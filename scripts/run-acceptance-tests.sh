#!/usr/bin/env bash

set -euo pipefail

cd oats/yaml
go install github.com/onsi/ginkgo/v2/ginkgo
export TESTCASE_SKIP_BUILD=true
export TESTCASE_BASE_PATH=..
ginkgo -v -r
