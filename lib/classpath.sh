#!/bin/bash

# Figure out script absolute path
pushd `dirname $0` > /dev/null
SCRIPT_DIR=`pwd`
popd > /dev/null

# The root dir is one up
ROOT_DIR=`dirname $SCRIPT_DIR`

# Create classpath using all classes from and dependencies of the `fuzz` and `instrument` packages

cp="$ROOT_DIR/target/classes"
for jar in $ROOT_DIR/lib/*.jar; do
  cp="$cp:$jar"
done
#
#cp="$cp:$ROOT_DIR/instrument/target/classes:$ROOT_DIR/instrument/target/test-classes"
#
#for jar in $ROOT_DIR/instrument/target/dependency/*.jar; do
#  cp="$cp:$jar"
#done

echo $cp
