#!/bin/bash

#cd "C:\Users\moude\IdeaProjects\myJQF"

PATH1="benchmarks/src/main/java/P1Constraint/"
#PATH1="tutorial/src/main/java/P10/"
FILES="P9ConstraintLogic.java P1Driver.java"
#FILES="intLogic.java intGenerator.java intTest.java"

PATH_TO_ROOT="../../../../../"

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
echo "file path: " $PATH1
echo "file names: " $FILES
echo "file name 2: " $FILE2
echo "file name 2: " $FILE2

echo "moving to directory: " $PATH1
cd $PATH1

javac -cp .:$(${PATH_TO_ROOT}jqf/scripts/classpath.sh):$(${PATH_TO_ROOT}mygraph/scripts/classpath.sh) ${FILES}


#cd "$PATH1"
${PATH_TO_ROOT}jqf/bin/jqf-mo -v -c .:$(${PATH_TO_ROOT}jqf/scripts/classpath.sh):$(${PATH_TO_ROOT}mygraph/scripts/classpath.sh) P9Driver test1
#../../../../jqf/bin/jqf-mo -v -c .:$(../../../../jqf/scripts/classpath.sh) intTest test