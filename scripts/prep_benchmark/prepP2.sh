#!/bin/bash

PATH1="benchmarks/src/main/java/P2Transportation/"
program_name="P2"

bash ./scripts/prepareBenchmark.sh $PATH1 $program_name

# Figure out script absolute path
repo_root=$(./scripts/repoRoot.sh)
echo "$repo_root"

