#!/bin/bash

PATH1="benchmarksFuzzable/P5/"
ProgramName="P5"
method=$2

DEFAULT_PATH="benchmarksFuzzable/"
#program_name=$1
#PATH1="${DEFAULT_PATH}${program_name}/"


class_name="${ProgramName}Driver"
class_method="test1"

PATH_TO_ROOT="$(./scripts/repoRoot.sh)/"

build_mvn=false
build_benchmark=false
copy_benchmark=false
while getopts 'mba:' OPTION; do
  case "$OPTION" in
    m)
      echo "MVN flag is set to true"
      build_mvn=true
      ;;
    b)
      echo "benchmark is compiled"
      build_benchmark=true
      ;;
    c)
      echo "benchmark is copied"
      copy_benchmark=true
      ;;
    ?)
      echo "script usage: $(basename \$0) [-l] [-h] [-a somevalue]" >&2
      exit 1
      ;;
  esac
done
shift "$(($OPTIND -1))"


echo "MVN will be build: " ${build_mvn}

if $build_mvn
then
mvn package
fi

echo "program will be copied: " ${copy_benchmark}

if $copy_benchmark
then
  echo "todo"
fi

echo "Configurations: "
echo "Working Directory: " "$PWD"
echo "MVN has been build: " ${build_mvn}
echo "file path: " $PATH1
echo "Method: " $method
echo "iterations: " $1

echo ""
if [[ ! $1 ]]
then
  echo "Iterations not provided"
  exit
fi

if [[ ! $2 ]]
then
  echo "method not provided"
  exit
fi


echo "moving to directory: " $PATH1
cd $PATH1 || exit

  javaFiles=`ls ./*.java`
  echo "Java files found in dir:"
  echo "${javaFiles}"
  echo ""
  javac -cp .:$(${PATH_TO_ROOT}jqf/scripts/classpath.sh):$(${PATH_TO_ROOT}mygraph/scripts/classpath.sh) ${javaFiles}


i=0
while [ $i -lt $1 ]
do
      echo "Start run"
#      ${PATH_TO_ROOT}jqf/bin/jqf-mo -v -c .:$(${PATH_TO_ROOT}jqf/scripts/classpath.sh):$(${PATH_TO_ROOT}mygraph/scripts/classpath.sh) $class_name $class_method
        ${PATH_TO_ROOT}jqf/bin/jqf-mo -v -c .:$(${PATH_TO_ROOT}jqf/scripts/classpath.sh):$(${PATH_TO_ROOT}mygraph/scripts/classpath.sh) $class_name $class_method PGFuzz 1 d10 -1 500
#      cp -r ./fuzz-dir/saved-inputs/ ./fuzz-dir/rand/saved-inputs_$i/
      mkdir -p ./fuzz-dir/${method}/saved-inputs_$i/
      cp -a ./fuzz-dir/saved-inputs/. ./fuzz-dir/${method}/saved-inputs_$i/
      i=$((i + 1))
done
#${PATH_TO_ROOT}jqf/bin/jqf-mo -v -c .:$(${PATH_TO_ROOT}jqf/scripts/classpath.sh):$(${PATH_TO_ROOT}mygraph/scripts/classpath.sh) $class_name $class_method
