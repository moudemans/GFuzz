#!/bin/bash

FUZZ_FOLDER="benchmarksFuzzable"
#Make dirs needed for fuzzing
if [ ! -d ${FUZZ_FOLDER} ]; then
  mkdir ${FUZZ_FOLDER}
  echo "Made fuzzable dir: ${FUZZ_FOLDER}"
  echo ""
fi

if [[ ! $1 ]]
then
  echo "Directory with java programs not provided"
  exit
fi

if [[ ! $2 ]]
then
  echo "Program name not provided"
  exit
fi

# Examples
#PATH1="benchmarks/src/main/java/P7Transportation/"
#program_name="P7"

PATH1=$1
program_name=$2
output_dir="${FUZZ_FOLDER}/${program_name}"

echo "Configurations: "
echo "Working Directory: " $PWD
echo "provided input dir: " $PATH1
echo "Program name: " $program_name
echo "Preparing fuzz folder at: $output_dir"
echo ""

#Make dirs needed for fuzzing
echo "Creating fuzz directories for ${program_name}..."
if [ ! -d ${output_dir} ]; then
  mkdir ${output_dir}
  echo "Made output dir: ${output_dir}"
fi

if [ ! -d "${output_dir}/fuzz-dir" ]; then
  mkdir "${output_dir}/fuzz-dir"
  echo "Made fuzz dir: ${output_dir}/fuzz-dir"

fi
if [ ! -d "${output_dir}/fuzz-dir/seeds" ]; then
  mkdir "${output_dir}/fuzz-dir/seeds"
  echo "Made seed folder in fuzz dir: ${output_dir}/fuzz-dir/seeds"
fi
echo ""

echo "Files in input directory"
for filename in $PATH1*.java; do
#   Collect the file name and place it in the new output dir
    xbase=${filename##*/}
    xfext=${xbase##*.}
    xpref=${xbase%.*}
    new_file_name="${output_dir}/${xpref}.${xfext}"


    echo "$filename" " --> " "${output_dir}/${xpref}.${xfext}"
    cp "$filename" "$new_file_name"

#    Remove first line containing package name. The class can't be in a package as it cannot be found by junit
    sed -i '1d'  "$new_file_name"
done



