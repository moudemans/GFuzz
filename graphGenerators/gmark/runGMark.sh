#!/bin/bash

cd src

if [[ $1 -gt 0 ]]
then
  for i in $(seq 1 $1);
  do
    ./test -c ../use-cases/P1.xml -g ../examples/P1/test-$i- -a
  done

else
  echo "No amount provided."
fi


