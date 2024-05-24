#!/bin/bash


cd gmark/src/
if [[ ! $1 ]]
then
  echo "Schema not provided"
  SCHEMA_PATH="use-cases/schema.xml"
else
  SCHEMA_PATH=$1
fi

if [[ ! $1 ]]
then
  echo "Schema not provided"
  SCHEMA_PATH="use-cases/schema.xml"
  DIR_PATH="default/output/"
else
  SCHEMA_PATH="../../benchmarksFuzzable/"$1"/fuzz-dir/GenSchema.xml"
  DIR_PATH="../../benchmarksFuzzable/"$1"/fuzz-dir/new-inputs/"

fi


if [[ ! $5 ]]
then
  echo "no output file name provided"
  FILE_NAME="test"
else
    FILE_NAME=$5
fi

echo "** Generating [${3}] DB states **"
echo "current working dir: " $(pwd)
echo "Schema: " $SCHEMA_PATH
echo "Output location: " ${DIR_PATH}${FILE_NAME}${i}".txt"
echo "Graph size: " $2


x=1
i=1

while [ $x -ge 1 ]
do
  file_count=$(find ${DIR_PATH} -name "*.txt" -type f | wc -l)
    echo -ne "files $file_count "\\r

  if [ $file_count -lt 100 ]
  then
      echo "Generating graph, current count $file_count"
#      echo "current working dir: " $(pwd)

        ./test -c ${SCHEMA_PATH} -g ${DIR_PATH}${FILE_NAME}-${i} -a
      i=$((i + 1))
  fi

done



