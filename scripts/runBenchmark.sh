#!/bin/bash


DEFAULT_PATH="benchmarksFuzzable/"
default_class_name="Driver"
default_class_method="test1"
default_mutation_method="PGFuzz"
default_new_inputs_enabled=1
default_duration="d5"
default_mutation_cat="-1"
default_mutation_depth=100

PATH_TO_ROOT="$(./scripts/repoRoot.sh)/"

build_mvn=false
mutation_method=$default_mutation_method

while getopts 'mba:' OPTION; do
  case "$OPTION" in
    m)
      echo "MVN flag is set to true"
      build_mvn=true
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

echo ""
if [[ ! $1 ]]
then
  echo "Iterations not provided as first argument"
  exit
fi

if [[ ! $2 ]]
then
  echo "output_name not provided as second arugment"
  exit
fi

if [[ ! $3 ]]
then
  echo "ProgramName not provided as third argument"
  exit
fi

if [[ ! $4 ]]
then
  echo "Method not provided as fourth argument, please use as defined in jqf/fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/mo/MoDriver.java"
  echo "Using default: " $default_mutation_method
  mutation_method=$default_mutation_method
else
  mutation_method=$4
fi

if [[ ! $5 ]]
then
  echo "new inputs not provided as fifth argument, please use as defined in jqf/fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/mo/MoDriver.java"
  echo "Using default: " $default_new_inputs_enabled
  new_inputs_enables=$default_new_inputs_enabled
else
  new_inputs_enables=$5
fi

if [[ ! $6 ]]
then
  echo "duration not provided as sixth argument, please use as defined in jqf/fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/mo/MoDriver.java"
  echo "Using default: " $default_duration
  duration=$default_duration
else
  duration=$6
fi

if [[ ! $7 ]]
then
  echo "mutation category not provided as seventh argument, please use as defined in jqf/fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/mo/MoDriver.java"
  echo "Using default: " $default_mutation_cat
  mutation_cat=$default_mutation_cat
else
  mutation_cat=$7
fi

if [[ ! $8 ]]
then
  echo "mutation depth not provided as eight argument, please use as defined in jqf/fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/mo/MoDriver.java"
  echo "Using default: " $default_mutation_depth
  mutation_depth=$default_mutation_depth
else
  mutation_depth=$8
fi

PATH1="${DEFAULT_PATH}/$3/"
ProgramName="$3"
output_name=$2

class_name="${ProgramName}${default_class_name}"
class_method=${default_class_method}

echo "************************"
echo "Configurations: "
echo "\t Working Directory: " "$PWD"
echo "\t MVN has been build: " ${build_mvn}
echo "\t file path: " $PATH1
echo "\t Method: " $output_name
echo "\t iterations: " $1
echo "\t class name: " $class_name
echo "\t method name: " $class_method
echo "\t output_name name: " $output_name
echo "\t Mutation method: " $mutation_method
echo "\t new inputs enables: " $new_inputs_enables
echo "\t duration: " $duration
echo "\t mutation cat (-1 = all): " $mutation_cat
echo "\t mutation_depth: " $mutation_depth


echo "moving to directory: " "$PATH1"
cd "$PATH1" || exit


# Build the java files
javaFiles=`ls ./*.java`
echo "Java files found in dir:"
echo "${javaFiles}"
echo ""
javac -cp .:$(${PATH_TO_ROOT}jqf/scripts/classpath.sh):$(${PATH_TO_ROOT}mygraph/scripts/classpath.sh) ${javaFiles}

# prepare output dir
output_dir="./fuzz-dir/${output_name}"
echo "Creating fuzz directories for ${output_dir}..."
if [ ! -d ${output_dir} ]; then
  mkdir ${output_dir}
else
  echo "Output directory already exists, please remove as the existing output will be overwritten"
  exit
fi


echo "Configurations: " > $output_dir"/run_log.txt"
echo "\t Working Directory: " "$PWD" >> $output_dir"/run_log.txt"
echo "\t MVN has been build: " ${build_mvn} >> $output_dir"/run_log.txt"
echo "\t file path: " $PATH1 >> $output_dir"/run_log.txt"
echo "\t Method: " $output_name >> $output_dir"/run_log.txt"
echo "\t iterations: " $1 >> $output_dir"/run_log.txt"
echo "\t class name: " $class_name >> $output_dir"/run_log.txt"
echo "\t method name: " $class_method >> $output_dir"/run_log.txt"
echo "\t output_name name: " $output_name >> $output_dir"/run_log.txt"
echo "\t Mutation method: " $mutation_method >> $output_dir"/run_log.txt"
echo "\t new inputs enables: " $new_inputs_enables >> $output_dir"/run_log.txt"
echo "\t duration: " $duration >> $output_dir"/run_log.txt"
echo "\t mutation cat (-1 = all): " $mutation_cat >> $output_dir"/run_log.txt"
echo "\t mutation_depth: " $mutation_depth >> $output_dir"/run_log.txt"

i=0
while [ $i -lt $1 ]
do
      echo "Start run: " $i
        "${PATH_TO_ROOT}"jqf/bin/jqf-mo -v -c .:$("${PATH_TO_ROOT}"jqf/scripts/classpath.sh):$("${PATH_TO_ROOT}"mygraph/scripts/classpath.sh) "$class_name" $class_method $mutation_method $new_inputs_enables $duration $mutation_cat $mutation_depth

      mkdir -p $output_dir/saved-inputs_$i/
      cp -a ./fuzz-dir/saved-inputs/. ${output_dir}/saved-inputs_$i/
      i=$((i + 1))
done

cd $PATH_TO_ROOT
