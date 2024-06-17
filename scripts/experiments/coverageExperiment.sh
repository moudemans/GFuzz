#!/bin/bash

output_name="compound3"
iterations=2

p=1
while [ $p -lt 10 ]
do
  ProgramName="P${p}"

  bash ./scripts/runBenchmark.sh $iterations $output_name $ProgramName

  p=$((p + 1))
done
