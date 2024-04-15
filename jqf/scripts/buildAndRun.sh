#!/bin/bash

#cd "C:\Users\moude\IdeaProjects\myJQF"

PATH1="tutorial/src/main/java/"
FILE1="int"
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

echo "$PWD"

javac -cp .:$(jqf/scripts/classpath.sh) ${PATH1}${FILE1}Logic.java ${PATH1}${FILE1}Generator.java ${PATH1}${FILE1}Test.java
echo "MVN will be build: " ${build_mvn}

if $build_mvn
then
mvn package
fi
cd "$PATH1"
../../../../jqf/bin/jqf-mo -v -c .:$(../../../../jqf/scripts/classpath.sh) ${FILE1}Test test
#../../../../jqf/bin/jqf-zest -v -c .:$(../../../../jqf/scripts/classpath.sh) ${FILE1}Test test