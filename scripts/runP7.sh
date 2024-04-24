#!/bin/bash

PATH1="benchmarks/src/main/java/P7Transportation/"
program_name="P7"

bash ./scripts/prepareBenchmark.sh $PATH1 $program_name

# Figure out script absolute path
repo_root=$(./scripts/repoRoot.sh)
echo $repo_root

