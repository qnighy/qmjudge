#!/bin/sh

judgement=$(cd $(dirname $0)/..;pwd)
current=/tmp/$$

probid=$1
langid=$2
probdir=$judgement/prob/$probid
langdir=$judgement/util/lang/$langid

if [ -e current ]; then rm -rf $current; fi
cp -r $judgement/current $current

timelimit=$(head -n 1 $probdir/ovs.txt | tail -n 1)
memlimit=$(head -n 2 $probdir/ovs.txt | tail -n 1)
programs=$(head -n 3 $probdir/ovs.txt | tail -n 1)
for program in $programs
do
  cd $current/$program
  cp -r $langdir/build/* .
  ./compile.sh
done

cd $current
cp -r $judgement/util/judge_lib/* .
cp -r $judgement/util/default_judge Judge
cp -r $langdir/run.txt .
cd Judge
./compile.sh
cd ..

for i in $(cd $probdir/data; ls|grep -E '^(0|[1-9][0-9]*)$')
do
  datadir=$probdir/data/$i
  cp -r $datadir/* .
  ./judge_time.sh $timelimit $memlimit $i
done

rm -r $current

