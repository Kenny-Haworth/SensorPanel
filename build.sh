#!/bin/bash
#
# Compiles the SensorPanel project into a single jar file.
#
# Usage: ./build.sh
#
# Output: SensorPanel.jar (in bin)

set -eu # add x for debug

# checkout the Forge if it doesn't exist or it's empty
if [[ ! -e lib/forge || ! $(ls lib/forge/* 2> /dev/null) ]]; then
    echo "The Forge is missing from your repository, assuming you forgot to clone with --recursive and fixing up your repo..."
    git submodule update --init --recursive
fi

# compile the Forge
./lib/forge/build.sh

# build the jar file list
classpath="."
for jar_file in $(find lib -type f -name "*.jar"); do
    classpath+=";$jar_file"
done

# compile all files
javac -Xlint:all,-serial \
      -d classes \
      -cp $classpath \
      $(find src -type f -name "*.java")

# copy all jars into the classes directory
find lib -type f -name "*.jar" -exec cp {} classes \;

# extract the content of all jar files
(
    cd classes
    for jar_file in $(find . -type f -name "*.jar"); do
        jar xf $jar_file
    done
)

# form a list of the class directories to place in the jar
class_dirs=""
for dir in $(cd classes && ls -d */ | cut -f1 -d'/'); do
    class_dirs+="-C classes $dir "
done

# create the jar
jar cfe bin/SensorPanel.jar src.SensorPanel $class_dirs res

# cleanup
rm -rf classes
echo "SensorPanel compiled successfully 🌡️"
