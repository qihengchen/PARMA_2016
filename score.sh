#!/bin/bash

alpha=$1
beta=$2
iter=$3

#run main
pkg="/Users/Qiheng/Desktop/Spring_2016/parma:/Users/Qiheng/Desktop/Spring_2016/parma/code:/Users/Qiheng/Desktop/Spring_2016/parma/PredArgAlignment-master/src:/Users/Qiheng/Documents/Packages/commons-math3-3.6/commons-math3-3.6.jar"
export CLASSPATH=$pkg
rm -rf code/*.class
javac code/*.java
output="/Users/Qiheng/Desktop/Spring_2016/parma/results.txt"
score="/Users/Qiheng/Desktop/Spring_2016/parma/Fscore.txt"
temp="/Users/Qiheng/Desktop/Spring_2016/parma/temp.txt"
#ser_path="/Users/Qiheng/Dropbox/wna_norms.ser"
ser_path="/Users/Qiheng/Dropbox/4_features_2445.ser"
java Main $output $ser_path $alpha $beta $iter

#score
cd PredArgAlignment-master/src
rm -rf core/*.class
javac /Users/Qiheng/Desktop/Spring_2016/parma/PredArgAlignment-master/src/core/*.java
evaluator="PredArgAlignment-master/src/core/Evaluator"
root="/Users/Qiheng/Desktop/Spring_2016/parma/PredArgAlignment-master/" #"PredArgAlignment-master" #"PredArgAlignment-master.src.code.Evaluator"
java core.Evaluator $root "hddcrp" $output $temp

#print results
echo "testing on: alpha="$alpha" beta="$beta" iter="$iter >> $score
echo $(cat $temp) >> $score
echo "testing on: alpha="$alpha" beta="$beta" iter="$iter
echo $(cat $temp)
rm -rf $temp