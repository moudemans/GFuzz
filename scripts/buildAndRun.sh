#!/bin/bash


#PATH1="benchmarks/src/main/java/P9Constraint/"
PATH1="benchmarksFuzzable/P7/"
ProgramName="P7"

class_name="${ProgramName}Driver"
class_method="test1"

PATH_TO_ROOT="$(./scripts/repoRoot.sh)/"

build_mvn=false

while getopts 'mha:' OPTION; do
  case "$OPTION" in
    m)
      echo "MVN flag is set to true"
      build_mvn=true
      ;;
    h)
      echo "you have supplied the -h option"
      ;;
    a)
      avalue="$OPTARG"
      echo "The value provided is $OPTARG"
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

echo "Configurations: "
echo "Working Directory: " $PWD
echo "MVN has been build: " ${build_mvn}
echo "file path: " $PATH1
echo ""

echo "moving to directory: " $PATH1
cd $PATH1

javaFiles=`ls ./*.java`
echo "Java files found in dir:"
echo "${javaFiles}"
echo ""

javac -cp .:$(${PATH_TO_ROOT}jqf/scripts/classpath.sh):$(${PATH_TO_ROOT}mygraph/scripts/classpath.sh) ${javaFiles}

${PATH_TO_ROOT}jqf/bin/jqf-mo -v -c .:$(${PATH_TO_ROOT}jqf/scripts/classpath.sh):$(${PATH_TO_ROOT}mygraph/scripts/classpath.sh) $class_name $class_method
