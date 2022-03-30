#!/bin/bash

cd universe

today=`date +%Y-%m-%d.%H:%M:%S` 
titl=Console_Log_${today}.txt

(java -cp ./out:$PWD/res/lib/JBend.jar:$PWD/res/lib/mysql-connector-java-8.0.19/mysql-connector-java-8.0.19.jar com/Main "$@") 2>&1 | tee logs/${titl}
