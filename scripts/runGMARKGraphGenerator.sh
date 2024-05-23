#!/bin/bash


cd gmark/src/
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

x=1
i=1
while [ $x -ge 1 ]
do
  file_count=$(find ../${DIR_PATH} -name "*.txt" -type f | wc -l)
    echo ../${DIR_PATH}
    echo "files $file_count"
    echo "i $i"
    echo $(pwd)

  if [ $file_count -lt 40 ]
  then
      echo "Generating graph"
#      ./pgMark $SCHEMA_PATH $2 --output=${DIR_PATH}${FILE_NAME}${i}".txt"

        ./test -c ../${SCHEMA_PATH} -g ../${DIR_PATH}${FILE_NAME}-${i} -a
      i=$((i + 1))
  fi

done


