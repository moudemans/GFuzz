#!/bin/bash

generatorPath="./graphGenerators/pgMark/"
relativeSource="../../../"

cd $generatorPath || echo "PGMark graph generator not found" | exit
if [[ ! $1 ]]
then
  echo "Program not provided, using default"
  SCHEMA_PATH="default/schema.xml"
  DIR_PATH="default/output/"

else
  SCHEMA_PATH=$relativeSource"benchmarksFuzzable/"$1"/fuzz-dir/GenSchema.xml"
  DIR_PATH=$relativeSource"benchmarksFuzzable/"$1"/fuzz-dir/new-inputs/"

fi

if [[ ! $2 ]]
then
  echo "Graph size not provided"
  exit
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
  file_count=$(find ${DIR_PATH} -name "*.csv" -type f | wc -l)
    echo -ne "Running generator for $1, files $file_count "\\r

  if [ $file_count -lt 100 ]
  then
      echo "Generating graph, current count $file_count"
      ./pgMark $SCHEMA_PATH $2 --output=${DIR_PATH}${FILE_NAME}${i}".csv"
      i=$((i + 1))
  fi

done


