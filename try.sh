#!/bin/bash

x=1
y=10
z=1
while [ $x -le $(($y)) ]
#for ((k=400;x<542;x+=2))
do
	while [ $z -le $(($y)) ]
	do
		echo z=$z
		z=$(($z+1))
	done

	echo x=$x
	x=$(($x+1))
done

alpha=3
incre_a=1
to_a=10
beta=1
incre_b=1
to_b=5
iter=10

num=$(($to_a-$alpha+$to_b-$beta))
echo $num tests -- $(($num*20/60)) min $(($num*20%60)) second

#echo 1.1 - 2.2 | bc -l
start=$SECONDS
echo $start
sleep 2
echo $((SECONDS-$start))
