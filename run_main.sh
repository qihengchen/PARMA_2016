#!/bin/bash


alpha=5
beta=0.1
iter=2

#run main
start=$SECONDS
export CLASSPATH="/Users/Qiheng/Desktop/Spring_2016/parma:/Users/Qiheng/Desktop/Spring_2016/parma/code:/Users/Qiheng/Documents/Packages/commons-math3-3.6/commons-math3-3.6.jar"
rm -rf code/*.class
javac code/*.java

#ser_path="/Users/Qiheng/Dropbox/wna_norms.ser"
ser_path="/Users/Qiheng/Dropbox/4_features_2445.ser"
java Main "output.txt" $ser_path $alpha $beta $iter
echo $((SECONDS-$start)) seconds passed