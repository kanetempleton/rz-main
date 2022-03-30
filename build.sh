#!/bin/bash

cd universe
javac -cp ./out:$PWD/res/lib/JBend.jar -d out $(find src/com/* | grep .java)

# https://stackoverflow.com/questions/5194926/compiling-java-files-in-all-subfolders
