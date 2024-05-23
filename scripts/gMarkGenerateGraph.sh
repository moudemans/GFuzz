#!/bin/bash


cd gMark/
if [[ ! $1 ]]
then
  echo "Schema not provided"
  SCHEMA_PATH="use-cases/schema.xml"
else
  SCHEMA_PATH=$1
fi

if [[ ! $2 ]]
then
  echo "Graph size not provided"
  exit
fi

if [[ ! $3 ]]
then
  echo "Amount of DB states not provided"
  exit
fi

if [[ ! $4 ]]
then
  echo "no output directory provided"
  DIR_PATH="out/"
else
  DIR_PATH=$4
fi

if [[ ! $5 ]]
then
  echo "no output file name provided"
  FILE_NAME="test"
else
    FILE_NAME=$5
fi

echo "** Generating [${3}] DB states **"
echo "Schema: " $SCHEMA_PATH
echo "Output location: " ${DIR_PATH}${FILE_NAME}${i}".txt"
echo "Graph size: " $2



for i in $(seq 1 $3);
do

    if [[ $1 -gt 0 ]]
    then
      for i in $(seq 1 $1);
      do
        ./test -c ../use-cases/P1.xml -g ../examples/P1/test-$i- -a
      done

    else
      echo "No amount provided."
    fi
done
