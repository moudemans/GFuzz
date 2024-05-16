#!/bin/bash


cd pgMark/
if [[ ! $1 ]]
then
  echo "Schema not provided"
  SCHEMA_PATH="default/schema.xml"
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
  DIR_PATH="default/output/"
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

x=1
i=1
while [ $x -ge 1 ]
do
  file_count=$(find ${DIR_PATH} -name "*.csv" -type f | wc -l)
    echo "files $file_count"

  if [ $file_count -lt 40 ]
  then
      echo "Generating graph"
      ./pgMark $SCHEMA_PATH $2 --output=${DIR_PATH}${FILE_NAME}${i}".csv"
      i=$((i + 1))
  fi

done


