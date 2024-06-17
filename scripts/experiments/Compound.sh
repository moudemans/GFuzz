#!/bin/bash

PATH1="benchmarksFuzzable/A1/"
ProgramName="A1"
method=$2

methods=("PGFuzz" "Random" "None")
output_name_dir="s500-"
i=0
while [ $i -lt 1 ]
do
    bash ./scripts/buildAndRun.sh 5 $output_name_dir${methods[$i]} "A1" PGFuzz

    i=$((i + 1))
done