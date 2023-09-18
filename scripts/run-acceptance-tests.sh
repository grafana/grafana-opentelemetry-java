#!/usr/bin/env bash

set -euo pipefail

cd oats/yaml
go install github.com/onsi/ginkgo/v2/ginkgo
export TESTCASE_SKIP_BUILD=true
export TESTCASE_TIMEOUT=2m
export TESTCASE_BASE_PATH=../../examples
docker network prune -f # not really sure why networks accumulate on github action runners
ginkgo -r # -p see if it works without parallel
