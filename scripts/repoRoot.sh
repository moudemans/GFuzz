#!/bin/bash

# Figure out script absolute path
pushd `dirname $0` > /dev/null
BIN_DIR=`pwd`
popd > /dev/null

ROOT_DIR=`dirname $BIN_DIR`
echo $ROOT_DIR