#!/bin/bash

alpha=3
incre_a=1
to_a=10
beta=1
incre_b=1
to_b=5
iter=1

start=$SECONDS
num=$((($to_a-$alpha)*($to_b-$beta)))
echo $num tests";"  estimated run"time" $(($num*25/60)) min $(($num*25%60)) second
sleep 3

rm -rf /Users/Qiheng/Desktop/Spring_2016/parma/Fscore.txt
while [ $alpha -le $(($to_a)) ]
do
	temp=$beta
	while [ $temp -le $(($to_b)) ]
	do
		./score.sh $alpha $temp $iter
		temp=$(($temp+$incre_b))
	done

	alpha=$(($alpha+$incre_a))
done

echo $((SECONDS-$start)) seconds passed

#use new features					2 min
#modify main to shorten runtime		8 min
#normalization in step 4			15 min
#try wide range of parameters		5 min
								#30min work + runtime