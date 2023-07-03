#!/bin/sh
echo "Working dir: $1"
echo "Project dir: $2"
echo "Sharpen args: $3"
echo "Running sharpen..."
cd "$1" && java -jar /sharpen/sharpencore-1.1.0-fork-jar-with-dependencies.jar $2 $3